package com.microlend.analytics.service;

import com.microlend.analytics.dto.*;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.collection.entity.CollectionRecord;
import com.microlend.collection.enums.CollectionStatus;
import com.microlend.collection.repository.CollectionRecordRepository;
import com.microlend.grouporigination.repository.BorrowerGroupRepository;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.entity.LoanApplication;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    @Value("${microlend.delinquency.par30-max-dpd:30}")
    private int par30MaxDpd;
    @Value("${microlend.delinquency.par60-max-dpd:60}")
    private int par60MaxDpd;
    @Value("${microlend.delinquency.par90-max-dpd:90}")
    private int par90MaxDpd;

    private final LoanAccountRepository loanAccountRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final CollectionRecordRepository collectionRepository;
    private final LoanApplicationRepository applicationRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowerGroupRepository groupRepository;
    private final UserRepository userRepository;

    // ---------------- Scope resolution ----------------

    /** Borrower IDs in scope, or null = system-wide (no filter). */
    private Set<Long> scopeBorrowerIds(Long userId, Role role, String scope) {
        boolean branchScope;
        if ("system".equalsIgnoreCase(scope)) {
            branchScope = false;
        } else if ("branch".equalsIgnoreCase(scope)) {
            branchScope = true;
        } else {
            branchScope = role != Role.NBFC_ADMIN; // default: admin=system, others=branch
        }
        if (!branchScope) {
            return null;
        }
        Long branchId = userRepository.findById(userId).map(User::getBranchId).orElse(null);
        if (branchId == null) {
            return null;
        }
        Set<Long> ids = new HashSet<>();
        for (User fo : userRepository.findByBranchIdAndRole(branchId, Role.FIELD_OFFICER)) {
            for (Borrower b : borrowerRepository.findByRegisteredByFieldOfficerId(fo.getUserId())) {
                ids.add(b.getBorrowerId());
            }
        }
        return ids;
    }

    private List<LoanAccount> scopedLoans(Set<Long> borrowerIds) {
        List<LoanAccount> all = loanAccountRepository.findAll();
        if (borrowerIds == null) {
            return all;
        }
        return all.stream().filter(a -> borrowerIds.contains(a.getBorrowerId())).toList();
    }

    // ---------------- 1. PAR distribution ----------------

    @Transactional(readOnly = true)
    public List<ParDistributionRow> parDistribution(Long userId, Role role, String scope) {
        List<LoanAccount> loans = scopedLoans(scopeBorrowerIds(userId, role, scope)).stream()
                .filter(a -> a.getStatus() == LoanAccountStatus.ACTIVE).toList();
        String[] buckets = {"CURRENT", "PAR30", "PAR60", "PAR90", "PAR180"};
        List<ParDistributionRow> rows = new ArrayList<>();
        for (String bucket : buckets) {
            long count = 0;
            BigDecimal outstanding = BigDecimal.ZERO;
            for (LoanAccount a : loans) {
                if (bucketFor(a.getDpd()).equals(bucket)) {
                    count++;
                    outstanding = outstanding.add(nz(a.getOutstandingPrincipal()));
                }
            }
            rows.add(new ParDistributionRow(bucket, count, outstanding));
        }
        return rows;
    }

    // ---------------- 2. Portfolio / disbursement trend ----------------

    @Transactional(readOnly = true)
    public List<PortfolioTrendRow> portfolioTrend(Long userId, Role role, String scope) {
        List<LoanAccount> loans = scopedLoans(scopeBorrowerIds(userId, role, scope));
        TreeMap<YearMonth, BigDecimal> byMonth = new TreeMap<>();
        for (LoanAccount a : loans) {
            if (a.getDisbursementDate() == null) continue;
            YearMonth ym = YearMonth.from(a.getDisbursementDate());
            byMonth.merge(ym, nz(a.getDisbursedAmount()), BigDecimal::add);
        }
        List<PortfolioTrendRow> rows = new ArrayList<>();
        BigDecimal cumulative = BigDecimal.ZERO;
        for (var e : byMonth.entrySet()) {
            cumulative = cumulative.add(e.getValue());
            rows.add(new PortfolioTrendRow(e.getKey().toString(), e.getValue(), cumulative));
        }
        return rows;
    }

    // ---------------- 3. Collection efficiency ----------------

    @Transactional(readOnly = true)
    public List<CollectionEfficiencyRow> collectionEfficiency(Long userId, Role role, String scope) {
        Set<Long> scopeLoanIds = scopedLoanIds(userId, role, scope);
        TreeMap<YearMonth, BigDecimal> dueByMonth = new TreeMap<>();
        for (RepaymentSchedule s : scheduleRepository.findAll()) {
            if (scopeLoanIds != null && !scopeLoanIds.contains(s.getLoanAccountId())) continue;
            if (s.getDueDate() == null) continue;
            dueByMonth.merge(YearMonth.from(s.getDueDate()), nz(s.getTotalDue()), BigDecimal::add);
        }
        TreeMap<YearMonth, BigDecimal> collectedByMonth = new TreeMap<>();
        for (CollectionRecord c : collectionRepository.findAll()) {
            if (c.getStatus() != CollectionStatus.CONFIRMED) continue; // only approved money counts
            if (scopeLoanIds != null && !scopeLoanIds.contains(c.getLoanAccountId())) continue;
            if (c.getCollectionDate() == null) continue;
            collectedByMonth.merge(YearMonth.from(c.getCollectionDate()), nz(c.getCollectedAmount()),
                    BigDecimal::add);
        }
        TreeMap<YearMonth, Boolean> months = new TreeMap<>();
        dueByMonth.keySet().forEach(m -> months.put(m, true));
        collectedByMonth.keySet().forEach(m -> months.put(m, true));
        List<CollectionEfficiencyRow> rows = new ArrayList<>();
        for (YearMonth m : months.keySet()) {
            BigDecimal due = dueByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal collected = collectedByMonth.getOrDefault(m, BigDecimal.ZERO);
            BigDecimal pct = due.signum() == 0 ? BigDecimal.ZERO
                    : collected.multiply(BigDecimal.valueOf(100)).divide(due, 2, RoundingMode.HALF_UP);
            rows.add(new CollectionEfficiencyRow(m.toString(), due, collected, pct));
        }
        return rows;
    }

    // ---------------- 4. Officer performance ----------------

    @Transactional(readOnly = true)
    public List<OfficerPerformanceRow> officerPerformance(Long userId, Role role, String scope) {
        List<User> officers;
        if (role == Role.BRANCH_MANAGER || "branch".equalsIgnoreCase(scope)) {
            Long branchId = userRepository.findById(userId).map(User::getBranchId).orElse(null);
            officers = branchId == null ? userRepository.findByRole(Role.FIELD_OFFICER)
                    : userRepository.findByBranchIdAndRole(branchId, Role.FIELD_OFFICER);
        } else {
            officers = userRepository.findByRole(Role.FIELD_OFFICER);
        }
        List<OfficerPerformanceRow> rows = new ArrayList<>();
        for (User fo : officers) {
            long borrowers = borrowerRepository.findByRegisteredByFieldOfficerId(fo.getUserId()).size();
            long groups = groupRepository.findByCreatedByFieldOfficerId(fo.getUserId()).size();
            List<CollectionRecord> collections = collectionRepository.findByCollectedById(fo.getUserId());
            long collectionCount = collections.size();
            BigDecimal collected = BigDecimal.ZERO;
            long confirmed = 0;
            for (CollectionRecord c : collections) {
                if (c.getStatus() == CollectionStatus.CONFIRMED) {
                    collected = collected.add(nz(c.getCollectedAmount()));
                    confirmed++;
                }
            }
            BigDecimal efficiency = collectionCount == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(confirmed * 100L)
                        .divide(BigDecimal.valueOf(collectionCount), 2, RoundingMode.HALF_UP);
            rows.add(new OfficerPerformanceRow(fo.getUserId(), fo.getName(), borrowers, groups,
                    collectionCount, collected, efficiency));
        }
        rows.sort(Comparator.comparing(OfficerPerformanceRow::collectedAmount).reversed());
        return rows;
    }

    // ---------------- 5. Loan funnel ----------------

    @Transactional(readOnly = true)
    public List<LoanFunnelRow> loanFunnel(Long userId, Role role, String scope) {
        Set<Long> scopeBorrowerIds = scopeBorrowerIds(userId, role, scope);
        String[] statuses = {"UNDER_ASSESSMENT", "WAITLISTED", "APPROVED", "SANCTIONED", "DISBURSED",
                "REJECTED"};
        List<LoanApplication> apps = applicationRepository.findAll().stream()
                .filter(a -> scopeBorrowerIds == null || scopeBorrowerIds.contains(a.getBorrowerId()))
                .toList();
        List<LoanFunnelRow> rows = new ArrayList<>();
        for (String st : statuses) {
            long count = apps.stream().filter(a -> a.getStatus().name().equals(st)).count();
            rows.add(new LoanFunnelRow(st, count));
        }
        return rows;
    }

    // ---------------- 6. NPA trend ----------------

    @Transactional(readOnly = true)
    public List<NpaTrendRow> npaTrend(Long userId, Role role, String scope) {
        List<LoanAccount> loans = scopedLoans(scopeBorrowerIds(userId, role, scope)).stream()
                .filter(a -> a.getStatus() == LoanAccountStatus.ACTIVE && a.getDisbursementDate() != null)
                .toList();
        if (loans.isEmpty()) {
            return List.of();
        }
        YearMonth start = loans.stream().map(a -> YearMonth.from(a.getDisbursementDate()))
                .min(Comparator.naturalOrder()).orElseThrow();
        YearMonth end = YearMonth.from(LocalDate.now());
        List<NpaTrendRow> rows = new ArrayList<>();
        for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            final YearMonth month = m;
            BigDecimal total = BigDecimal.ZERO;
            BigDecimal npa = BigDecimal.ZERO;
            for (LoanAccount a : loans) {
                if (YearMonth.from(a.getDisbursementDate()).isAfter(month)) continue;
                total = total.add(nz(a.getOutstandingPrincipal()));
                if (a.getDpd() != null && a.getDpd() > par90MaxDpd) {
                    npa = npa.add(nz(a.getOutstandingPrincipal()));
                }
            }
            BigDecimal pct = total.signum() == 0 ? BigDecimal.ZERO
                    : npa.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
            rows.add(new NpaTrendRow(month.toString(), pct, npa, total));
        }
        return rows;
    }

    // ---------------- helpers ----------------

    private Set<Long> scopedLoanIds(Long userId, Role role, String scope) {
        Set<Long> borrowerIds = scopeBorrowerIds(userId, role, scope);
        if (borrowerIds == null) {
            return null;
        }
        Set<Long> loanIds = new HashSet<>();
        for (LoanAccount a : loanAccountRepository.findAll()) {
            if (borrowerIds.contains(a.getBorrowerId())) {
                loanIds.add(a.getLoanAccountId());
            }
        }
        return loanIds;
    }

    private String bucketFor(Integer dpd) {
        int d = dpd == null ? 0 : dpd;
        if (d <= 0) return "CURRENT";
        if (d <= par30MaxDpd) return "PAR30";
        if (d <= par60MaxDpd) return "PAR60";
        if (d <= par90MaxDpd) return "PAR90";
        return "PAR180";
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
