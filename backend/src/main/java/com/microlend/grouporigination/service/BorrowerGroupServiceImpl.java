package com.microlend.grouporigination.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.grouporigination.dto.GroupRequest;
import com.microlend.grouporigination.dto.GroupResponse;
import com.microlend.grouporigination.dto.GroupSummaryResponse;
import com.microlend.grouporigination.entity.BorrowerGroup;
import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.enums.CommonStatus;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerGroupServiceImpl implements BorrowerGroupService {

    private final BorrowerGroupRepository groupRepository;
    private final BorrowerRepository borrowerRepository;
    private final CentreService centreService;
    private final LoanAccountRepository loanAccountRepository;
    private final RepaymentScheduleRepository scheduleRepository;

    @Transactional
    public GroupResponse create(Long officerId, GroupRequest req) {
        // Centre must belong to this officer.
        Centre centre = centreService.getOwnedCentre(officerId, req.centreId());

        BorrowerGroup group = BorrowerGroup.builder()
                .groupName(req.groupName())
                .centreId(centre.getCentreId())
                .createdByFieldOfficerId(officerId)
                .formationDate(LocalDate.now())
                .jointLiabilityEnabled(req.jointLiabilityEnabled() == null || req.jointLiabilityEnabled())
                .memberCount(0)
                .status(CommonStatus.ACTIVE)
                .build();
        group = groupRepository.save(group);

        // Optional member selection — restricted to borrowers this officer registered.
        int count = 0;
        if (req.memberBorrowerIds() != null && !req.memberBorrowerIds().isEmpty()) {
            for (Long borrowerId : req.memberBorrowerIds()) {
                Borrower b = borrowerRepository.findById(borrowerId)
                        .orElseThrow(() -> ApiException.badRequest("Borrower not found: " + borrowerId));
                if (!b.getRegisteredByFieldOfficerId().equals(officerId)) {
                    throw ApiException.forbidden("Borrower " + borrowerId + " not registered by this officer");
                }
                b.setGroupId(group.getGroupId());
                b.setCentreId(centre.getCentreId());
                b.setBorrowerType(BorrowerType.GROUP);
                borrowerRepository.save(b);
                count++;
            }
        }
        group.setMemberCount(count);
        group = groupRepository.save(group);
        return GroupResponse.from(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> listForOfficer(Long officerId) {
        return groupRepository.findByCreatedByFieldOfficerId(officerId).stream()
                .map(GroupResponse::from).toList();
    }

    @Override
    @Transactional
    public GroupResponse update(Long officerId, Long groupId, GroupRequest req) {
        BorrowerGroup g = getOwnedGroup(officerId, groupId);
        g.setGroupName(req.groupName());
        if (req.jointLiabilityEnabled() != null) {
            g.setJointLiabilityEnabled(req.jointLiabilityEnabled());
        }
        // Membership is managed at registration / creation; update only renames + toggles JLG.
        return GroupResponse.from(groupRepository.save(g));
    }

    @Override
    @Transactional
    public void delete(Long officerId, Long groupId) {
        BorrowerGroup g = getOwnedGroup(officerId, groupId);
        if (!borrowerRepository.findByGroupId(groupId).isEmpty()) {
            throw ApiException.badRequest("Cannot delete a group that still has members. Remove members first.");
        }
        groupRepository.delete(g);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupSummaryResponse summary(Long officerId, Long groupId) {
        BorrowerGroup g = getOwnedGroup(officerId, groupId);
        List<Borrower> members = borrowerRepository.findByGroupId(groupId);

        List<GroupSummaryResponse.MemberSummary> memberRows = new ArrayList<>();
        BigDecimal totalDisbursed = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        long overdueMembers = 0;

        for (Borrower m : members) {
            List<LoanAccount> loans = loanAccountRepository.findByBorrowerId(m.getBorrowerId());
            long active = 0;
            BigDecimal outstanding = BigDecimal.ZERO;
            boolean hasOverdue = false;
            for (LoanAccount la : loans) {
                totalDisbursed = totalDisbursed.add(nz(la.getDisbursedAmount()));
                if (la.getStatus() == LoanAccountStatus.ACTIVE) {
                    active++;
                    outstanding = outstanding.add(nz(la.getOutstandingPrincipal()));
                }
                for (RepaymentSchedule s : scheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNumberAsc(la.getLoanAccountId())) {
                    if (s.getStatus() == ScheduleStatus.OVERDUE) {
                        hasOverdue = true;
                        break;
                    }
                }
            }
            totalOutstanding = totalOutstanding.add(outstanding);
            if (hasOverdue) {
                overdueMembers++;
            }
            memberRows.add(new GroupSummaryResponse.MemberSummary(
                    m.getBorrowerId(), m.getName(), active, outstanding, hasOverdue));
        }

        return new GroupSummaryResponse(g.getGroupId(), g.getGroupName(), g.getCentreId(),
                members.size(), g.isJointLiabilityEnabled(), totalDisbursed, totalOutstanding,
                overdueMembers, memberRows);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    @Transactional
    public BorrowerGroup getOwnedGroup(Long officerId, Long groupId) {
        BorrowerGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> ApiException.notFound("Group not found: " + groupId));
        if (!g.getCreatedByFieldOfficerId().equals(officerId)) {
            throw ApiException.forbidden("Group does not belong to this officer");
        }
        return g;
    }

    @Transactional
    public void refreshMemberCount(Long groupId) {
        BorrowerGroup g = groupRepository.findById(groupId)
                .orElseThrow(() -> ApiException.notFound("Group not found: " + groupId));
        g.setMemberCount(borrowerRepository.findByGroupId(groupId).size());
        groupRepository.save(g);
    }
}
