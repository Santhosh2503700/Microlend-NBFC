package com.microlend.loan.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.loan.dto.SanctionLetterResponse;
import com.microlend.loan.entity.*;
import com.microlend.loan.enums.*;
import com.microlend.loan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class SanctionAcceptanceServiceImpl implements SanctionAcceptanceService {

    private final SanctionLetterRepository sanctionLetterRepository;
    private final LoanApplicationRepository applicationRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanProductRepository productRepository;
    private final BorrowerRepository borrowerRepository;
    private final EmiCalculationService emiService;
    private final AuditGateway auditService;

    @Transactional(readOnly = true)
    public List<SanctionLetterResponse> listForBorrower(Long borrowerUserId) {
        Borrower borrower = borrowerRepository.findByPortalUserId(borrowerUserId)
                .orElseThrow(() -> ApiException.forbidden("No borrower profile for this user"));
        List<LoanApplication> apps = applicationRepository.findByBorrowerId(borrower.getBorrowerId());
        List<Long> appIds = apps.stream().map(LoanApplication::getApplicationId).toList();
        if (appIds.isEmpty()) {
            return List.of();
        }
        return sanctionLetterRepository.findByApplicationIdIn(appIds).stream()
                .map(s -> SanctionLetterResponse.from(s, borrower.getBorrowerId(), borrower.getName()))
                .toList();
    }

    @Transactional
    public Object accept(Long borrowerUserId, Long sanctionId) {
        SanctionLetter letter = loadOwned(borrowerUserId, sanctionId);
        if (letter.getStatus() != SanctionStatus.ISSUED) {
            throw ApiException.badRequest("Sanction letter is not in ISSUED state (" + letter.getStatus() + ")");
        }
        LoanApplication application = applicationRepository.findById(letter.getApplicationId()).orElseThrow();
        LoanProduct product = productRepository.findById(application.getLoanProductId()).orElseThrow();

        assertNoOverdueCoMember(application);

        // 1) Accept the letter.
        letter.setAcceptedByBorrower(true);
        letter.setStatus(SanctionStatus.ACCEPTED);
        sanctionLetterRepository.save(letter);

        // 2) Build the amortization from the shared engine on the REAL sanctioned amount.
        AmortizationResult am = emiService.amortizationSchedule(
                letter.getSanctionedAmount(), letter.getInterestRate(), letter.getTenure(),
                product.getInterestType());

        // 3) Create the LoanAccount (disbursement).
        LocalDate disbursementDate = LocalDate.now();
        LoanAccount account = LoanAccount.builder()
                .applicationId(application.getApplicationId())
                .borrowerId(application.getBorrowerId())
                .productId(product.getProductId())
                .disbursedAmount(letter.getSanctionedAmount())
                .disbursementDate(disbursementDate)
                .totalInterest(am.getTotalInterest())
                .totalRepayable(am.getTotalRepayable())
                .outstandingPrincipal(letter.getSanctionedAmount())
                .dpd(0)
                .status(LoanAccountStatus.ACTIVE)
                .build();
        account = loanAccountRepository.save(account);

        // 4) Generate the full RepaymentSchedule (one row per installment).
        List<RepaymentSchedule> rows = new ArrayList<>();
        for (AmortizationRow r : am.getRows()) {
            rows.add(RepaymentSchedule.builder()
                    .loanAccountId(account.getLoanAccountId())
                    .installmentNumber(r.getInstallmentNumber())
                    .dueDate(disbursementDate.plusMonths(r.getInstallmentNumber()))
                    .principalDue(r.getPrincipalDue())
                    .interestDue(r.getInterestDue())
                    .totalDue(r.getTotalDue())
                    .status(ScheduleStatus.PENDING)
                    .build());
        }
        scheduleRepository.saveAll(rows);

        application.setStatus(ApplicationStatus.DISBURSED);
        applicationRepository.save(application);

        Borrower borrower = borrowerRepository.findById(application.getBorrowerId()).orElseThrow();
        auditService.record(borrowerUserId, "SANCTION_ACCEPTED_LOAN_DISBURSED", "LOAN_ACCOUNT",
                "loanAccountId=" + account.getLoanAccountId() + " disbursed=" + account.getDisbursedAmount()
                        + " installments=" + rows.size());

        return java.util.Map.of(
                "message", "Sanction accepted. Loan disbursed and repayment schedule generated.",
                "loanAccountId", account.getLoanAccountId(),
                "disbursedAmount", account.getDisbursedAmount(),
                "totalRepayable", account.getTotalRepayable(),
                "totalInterest", account.getTotalInterest(),
                "installments", rows.size(),
                "firstDueDate", rows.get(0).getDueDate().toString(),
                "emiAmount", letter.getEmiAmount(),
                "borrowerName", borrower.getName()
        );
    }

    @Transactional
    public SanctionLetterResponse reject(Long borrowerUserId, Long sanctionId) {
        SanctionLetter letter = loadOwned(borrowerUserId, sanctionId);
        if (letter.getStatus() != SanctionStatus.ISSUED) {
            throw ApiException.badRequest("Sanction letter is not in ISSUED state (" + letter.getStatus() + ")");
        }
        letter.setStatus(SanctionStatus.LAPSED);
        sanctionLetterRepository.save(letter);
        auditService.record(borrowerUserId, "SANCTION_REJECTED", "SANCTION_LETTER", "sanctionId=" + sanctionId);
        Borrower borrower = borrowerRepository.findByPortalUserId(borrowerUserId).orElseThrow();
        return SanctionLetterResponse.from(letter, borrower.getBorrowerId(), borrower.getName());
    }

    private void assertNoOverdueCoMember(LoanApplication application) {
        Long groupId = application.getGroupId();
        if (groupId == null) {
            return;
        }
        for (Borrower member : borrowerRepository.findByGroupId(groupId)) {
            if (member.getBorrowerId().equals(application.getBorrowerId())) {
                continue; // the applicant themselves
            }
            for (LoanAccount la : loanAccountRepository.findByBorrowerId(member.getBorrowerId())) {
                if (la.getStatus() != LoanAccountStatus.ACTIVE) {
                    continue;
                }
                boolean overdue = scheduleRepository
                        .findByLoanAccountIdOrderByInstallmentNumberAsc(la.getLoanAccountId())
                        .stream().anyMatch(s -> s.getStatus() == ScheduleStatus.OVERDUE);
                if (overdue) {
                    throw ApiException.badRequest(
                            "Joint-liability hold: group member '" + member.getName() + "' has overdue dues. "
                                    + "All group members must be current before a new disbursement.");
                }
            }
        }
    }

    private SanctionLetter loadOwned(Long borrowerUserId, Long sanctionId) {
        SanctionLetter letter = sanctionLetterRepository.findById(sanctionId)
                .orElseThrow(() -> ApiException.notFound("Sanction letter not found: " + sanctionId));
        LoanApplication application = applicationRepository.findById(letter.getApplicationId())
                .orElseThrow(() -> ApiException.notFound("Application not found"));
        Borrower borrower = borrowerRepository.findById(application.getBorrowerId())
                .orElseThrow(() -> ApiException.notFound("Borrower not found"));
        if (!borrowerUserId.equals(borrower.getPortalUserId())) {
            throw ApiException.forbidden("This sanction letter does not belong to you");
        }
        return letter;
    }
}
