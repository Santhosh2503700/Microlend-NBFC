package com.microlend.delinquency.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.BorrowerStatus;
import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.enums.Gender;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.delinquency.dto.BranchStaffResponse;
import com.microlend.delinquency.dto.DelinquencyCaseResponse;
import com.microlend.delinquency.dto.ScanResultResponse;
import com.microlend.delinquency.enums.CaseStatus;
import com.microlend.delinquency.entity.DelinquencyCase;
import com.microlend.delinquency.enums.ParBucket;
import com.microlend.delinquency.repository.DelinquencyCaseRepository;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.notification.enums.NotificationCategory;
import com.microlend.notification.service.NotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class DelinquencyServiceImpl implements DelinquencyService {

    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final DelinquencyCaseRepository caseRepository;
    private final BorrowerRepository borrowerRepository;
    private final LoanProductRepository loanProductRepository;
    private final UserRepository userRepository;
    private final NotificationGateway notificationGateway;
    private final AuditGateway auditService;

    static final String DEMO_PREFIX = "DEMO — ";

    @Value("${microlend.delinquency.par30-max-dpd:30}")
    private int par30MaxDpd;
    @Value("${microlend.delinquency.par60-max-dpd:60}")
    private int par60MaxDpd;
    @Value("${microlend.delinquency.par90-max-dpd:90}")
    private int par90MaxDpd;

    // ----------------------------------------------------------------
    // Scan (scheduled daily / triggerable on demand)
    // ----------------------------------------------------------------

    @Transactional
    public ScanResultResponse runScan(Long triggeredByUserId) {
        LocalDate today = LocalDate.now();

        // Flip every past-due Pending installment to Overdue (system-wide).
        List<RepaymentSchedule> newlyOverdue =
                scheduleRepository.findByStatusAndDueDateBefore(ScheduleStatus.PENDING, today);
        for (RepaymentSchedule s : newlyOverdue) {
            s.setStatus(ScheduleStatus.OVERDUE);
        }
        scheduleRepository.saveAll(newlyOverdue);

        // Recompute DPD per active account and open/update/close cases accordingly.
        List<LoanAccount> accounts = loanAccountRepository.findByStatus(LoanAccountStatus.ACTIVE);
        int opened = 0, updated = 0, resolved = 0;
        for (LoanAccount account : accounts) {
            CaseOutcome outcome = recomputeAccount(account, today, true);
            switch (outcome) {
                case OPENED -> opened++;
                case UPDATED -> updated++;
                case RESOLVED -> resolved++;
                default -> { /* no change */ }
            }
        }

        ScanResultResponse result = new ScanResultResponse(
                newlyOverdue.size(), accounts.size(), opened, updated, resolved);
        auditService.record(triggeredByUserId, "DELINQUENCY_SCAN", "DELINQUENCY",
                "overdue=" + newlyOverdue.size() + " accounts=" + accounts.size()
                        + " opened=" + opened + " updated=" + updated + " resolved=" + resolved);
        log.info("Delinquency scan complete: {}", result);
        return result;
    }


    @Transactional
    public void onLoanPaymentApplied(Long loanAccountId) {
        loanAccountRepository.findById(loanAccountId)
                .ifPresent(account -> recomputeAccount(account, LocalDate.now(), false));
    }

    @Override
    @Transactional
    public Map<String, Object> backdateEarliestUnpaidInstallment(Long loanAccountId, int days) {
        loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> ApiException.notFound("Loan account not found: " + loanAccountId));
        RepaymentSchedule target = scheduleRepository
                .findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccountId).stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("No unpaid installment to backdate on this loan"));
        LocalDate newDue = LocalDate.now().minusDays(Math.max(days, 1));
        target.setDueDate(newDue);
        scheduleRepository.save(target);
        return Map.of(
                "loanAccountId", loanAccountId,
                "scheduleId", target.getScheduleId(),
                "installmentNumber", target.getInstallmentNumber(),
                "newDueDate", newDue.toString(),
                "message", "Installment backdated. Run the delinquency scan to open a case.");
    }

    // ----------------------------------------------------------------
    // DEMO / dev tooling — seed a portfolio spread across PAR buckets, then scan
    // ----------------------------------------------------------------


    @Override
    @Transactional
    public Map<String, Object> generateDemoPortfolioAndScan(Long adminUserId) {
        // Resolve a field officer whose branch has the seeded Branch Manager, so the opened cases
        // are visible to that BM (case→branch walks Borrower.registeredByFieldOfficerId → branchId).
        User fieldOfficer = userRepository.findByRole(Role.FIELD_OFFICER).stream().findFirst()
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getBranchId() != null).findFirst())
                .orElseThrow(() -> ApiException.badRequest(
                        "No field officer (or any branch-attached user) exists to own the demo portfolio"));
        Long fieldOfficerId = fieldOfficer.getUserId();

        purgePriorDemoData();

        // Reuse an existing product id when one exists; the column is non-null but has no FK, so a
        // demo placeholder is safe on a fresh database with no products.
        Long productId = loanProductRepository.findAll().stream()
                .findFirst().map(LoanProduct::getProductId).orElse(0L);

        LocalDate today = LocalDate.now();
        int[] targetDpds = {15, 45, 75, 120};
        String[] bucketLabels = {"PAR30", "PAR60", "PAR90", "PAR180"};

        for (int i = 0; i < targetDpds.length; i++) {
            int targetDpd = targetDpds[i];
            String label = bucketLabels[i];

            Borrower borrower = Borrower.builder()
                    .name(DEMO_PREFIX + label + " Borrower")
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.FEMALE)
                    .nationalIdNumber(String.format("9990000%05d", i))   // 12 digits, unique per index
                    .village("Demo")
                    .district("Demo")
                    .phone(String.format("888000%04d", i))               // 10 digits, unique per index
                    .occupation("Demo")
                    .monthlyIncome(new BigDecimal("20000.00"))
                    .bankAccountNumber("123456789")
                    .ifscCode("HDFC0001234")
                    .status(BorrowerStatus.ACTIVE)
                    .registeredByFieldOfficerId(fieldOfficerId)
                    .borrowerType(BorrowerType.INDIVIDUAL)
                    .centreId(1L)          // non-null column, no FK — demo placeholder
                    .portalUserId(null)
                    .build();
            borrower = borrowerRepository.save(borrower);

            LoanAccount account = LoanAccount.builder()
                    .applicationId(0L)     // non-null column, no FK — demo placeholder
                    .borrowerId(borrower.getBorrowerId())
                    .productId(productId)
                    .disbursedAmount(new BigDecimal("10000.00"))
                    .disbursementDate(today.minusMonths(6))
                    .totalInterest(new BigDecimal("1200.00"))
                    .totalRepayable(new BigDecimal("11200.00"))
                    .outstandingPrincipal(new BigDecimal("10000.00"))
                    .dpd(0)
                    .status(LoanAccountStatus.ACTIVE)
                    .build();
            account = loanAccountRepository.save(account);

            RepaymentSchedule schedule = RepaymentSchedule.builder()
                    .loanAccountId(account.getLoanAccountId())
                    .installmentNumber(1)
                    .dueDate(today.minusDays(targetDpd))   // earliest unpaid, past-due → scan flags it
                    .principalDue(new BigDecimal("5000.00"))
                    .interestDue(new BigDecimal("600.00"))
                    .totalDue(new BigDecimal("5600.00"))
                    .status(ScheduleStatus.PENDING)
                    .build();
            scheduleRepository.save(schedule);
        }

        // Hand off to the real engine: it re-ages installments, computes DPD, buckets and opens cases.
        ScanResultResponse scan = runScan(adminUserId);

        auditService.record(adminUserId, "DELINQUENCY_DEMO_SEED", "DELINQUENCY",
                "loansCreated=" + targetDpds.length + " opened=" + scan.casesOpened());

        Map<String, Object> buckets = new LinkedHashMap<>();
        for (String label : bucketLabels) {
            buckets.put(label, 1);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Seeded " + targetDpds.length
                + " demo loans across PAR30/60/90/180 and ran the scan.");
        result.put("loansCreated", targetDpds.length);
        result.put("scan", scan);
        result.put("buckets", buckets);
        return result;
    }

    // Deletes all demo data in FK-safe order: cases → schedules → accounts → borrowers.
    private void purgePriorDemoData() {
        List<Borrower> demoBorrowers = borrowerRepository.findByNameStartingWith(DEMO_PREFIX);
        for (Borrower b : demoBorrowers) {
            for (LoanAccount account : loanAccountRepository.findByBorrowerId(b.getBorrowerId())) {
                caseRepository.deleteAll(caseRepository.findByLoanAccountId(account.getLoanAccountId()));
                scheduleRepository.deleteAll(
                        scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(account.getLoanAccountId()));
                loanAccountRepository.delete(account);
            }
        }
        borrowerRepository.deleteAll(demoBorrowers);
    }

    private enum CaseOutcome { NONE, OPENED, UPDATED, RESOLVED }


    private CaseOutcome recomputeAccount(LoanAccount account, LocalDate today, boolean allowCreate) {
        int dpd = computeDpd(account.getLoanAccountId(), today);
        account.setDpd(dpd);
        loanAccountRepository.save(account);

        Optional<DelinquencyCase> existing = caseRepository
                .findFirstByLoanAccountIdAndStatusNot(account.getLoanAccountId(), CaseStatus.RESOLVED);

        if (dpd > 0) {
            ParBucket bucket = bucketFor(dpd);
            if (existing.isPresent()) {
                DelinquencyCase c = existing.get();
                boolean changed = !c.getDpd().equals(dpd) || c.getParBucket() != bucket;
                c.setDpd(dpd);
                c.setParBucket(bucket);
                caseRepository.save(c);
                return changed ? CaseOutcome.UPDATED : CaseOutcome.NONE;
            }
            if (allowCreate) {
                openCase(account, dpd, bucket);
                return CaseOutcome.OPENED;
            }
            return CaseOutcome.NONE;
        }

        // dpd == 0: nothing overdue — close any open case.
        if (existing.isPresent()) {
            resolveCase(existing.get(), account);
            return CaseOutcome.RESOLVED;
        }
        return CaseOutcome.NONE;
    }

    // DPD = days since the oldest still-unpaid, past-due installment fell due (0 if none).
    private int computeDpd(Long loanAccountId, LocalDate today) {
        return scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccountId).stream()
                .filter(s -> s.getStatus() != ScheduleStatus.PAID)
                .filter(s -> s.getDueDate().isBefore(today))
                .map(RepaymentSchedule::getDueDate)
                .min(LocalDate::compareTo)
                .map(oldest -> (int) ChronoUnit.DAYS.between(oldest, today))
                .orElse(0);
    }

    private ParBucket bucketFor(int dpd) {
        if (dpd <= 0) return ParBucket.CURRENT;
        if (dpd <= par30MaxDpd) return ParBucket.PAR30;
        if (dpd <= par60MaxDpd) return ParBucket.PAR60;
        if (dpd <= par90MaxDpd) return ParBucket.PAR90;
        return ParBucket.PAR180;
    }

    private void openCase(LoanAccount account, int dpd, ParBucket bucket) {
        Borrower borrower = borrowerRepository.findById(account.getBorrowerId()).orElse(null);
        Long bmId = branchManagerFor(borrower);

        DelinquencyCase c = DelinquencyCase.builder()
                .loanAccountId(account.getLoanAccountId())
                .dpd(dpd)
                .parBucket(bucket)
                .status(CaseStatus.OPEN)
                .openedDate(LocalDateTime.now())
                .action("Auto-opened by delinquency scan (DPD=" + dpd + ", " + bucket + ")")
                .notifiedBranchManagerId(bmId)
                .build();
        c = caseRepository.save(c);

        String borrowerName = borrower == null ? "Unknown" : borrower.getName();
        auditService.record(null, "DELINQUENCY_CASE_OPENED", "DELINQUENCY",
                "caseId=" + c.getCaseId() + " loanAccountId=" + account.getLoanAccountId()
                        + " dpd=" + dpd + " bucket=" + bucket);

        if (bmId != null) {
            notificationGateway.notifyUser(bmId, Role.BRANCH_MANAGER.name(),
                    "Delinquency case #" + c.getCaseId() + " opened for borrower " + borrowerName
                            + " (loan #" + account.getLoanAccountId() + "): DPD " + dpd + ", " + bucket,
                    NotificationCategory.DELINQUENCY, "DelinquencyCase", c.getCaseId());
        } else {
            log.warn("No Branch Manager found to notify for delinquency case {} (loan {})",
                    c.getCaseId(), account.getLoanAccountId());
        }
    }

    private void resolveCase(DelinquencyCase c, LoanAccount account) {
        c.setStatus(CaseStatus.RESOLVED);
        c.setDpd(0);
        c.setParBucket(ParBucket.CURRENT);
        c.setAction((c.getAction() == null ? "" : c.getAction() + " | ")
                + "Auto-resolved: all overdue installments cleared");
        caseRepository.save(c);

        String borrowerName = borrowerRepository.findById(account.getBorrowerId())
                .map(Borrower::getName).orElse("Unknown");
        auditService.record(null, "DELINQUENCY_CASE_RESOLVED", "DELINQUENCY",
                "caseId=" + c.getCaseId() + " loanAccountId=" + account.getLoanAccountId());

        String msg = "Delinquency case #" + c.getCaseId() + " for borrower " + borrowerName
                + " (loan #" + account.getLoanAccountId() + ") resolved — all overdue installments cleared";
        if (c.getNotifiedBranchManagerId() != null) {
            notificationGateway.notifyUser(c.getNotifiedBranchManagerId(), Role.BRANCH_MANAGER.name(),
                    msg, NotificationCategory.DELINQUENCY, "DelinquencyCase", c.getCaseId());
        }
        if (c.getAssignedCollectionsOfficerId() != null) {
            notificationGateway.notifyUser(c.getAssignedCollectionsOfficerId(), Role.COLLECTIONS_OFFICER.name(),
                    msg, NotificationCategory.DELINQUENCY, "DelinquencyCase", c.getCaseId());
        }
    }

    // ----------------------------------------------------------------
    // Branch Manager views + assignment
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DelinquencyCaseResponse> listOpenCasesForBranch(Long managerUserId) {
        Long branchId = branchOfUser(managerUserId);
        if (branchId == null) {
            throw ApiException.forbidden("Manager is not attached to a branch");
        }
        return caseRepository.findByStatusNot(CaseStatus.RESOLVED).stream()
                .filter(c -> branchId.equals(branchOfLoanAccount(c.getLoanAccountId())))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BranchStaffResponse> collectionsOfficersInBranch(Long managerUserId) {
        Long branchId = branchOfUser(managerUserId);
        if (branchId == null) {
            throw ApiException.forbidden("Manager is not attached to a branch");
        }
        return userRepository.findByBranchIdAndRole(branchId, Role.COLLECTIONS_OFFICER).stream()
                .map(BranchStaffResponse::from)
                .toList();
    }

    @Transactional
    public DelinquencyCaseResponse assignOfficer(Long managerUserId, Long caseId, Long officerId) {
        Long branchId = branchOfUser(managerUserId);
        if (branchId == null) {
            throw ApiException.forbidden("Manager is not attached to a branch");
        }
        DelinquencyCase c = caseRepository.findById(caseId)
                .orElseThrow(() -> ApiException.notFound("Delinquency case not found: " + caseId));

        // Case must be in the manager's branch.
        if (!branchId.equals(branchOfLoanAccount(c.getLoanAccountId()))) {
            throw ApiException.forbidden("Case does not belong to your branch");
        }
        if (c.getStatus() == CaseStatus.RESOLVED) {
            throw ApiException.badRequest("Cannot assign an officer to a resolved case");
        }

        // Officer must be a Collections Officer in the same branch.
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> ApiException.notFound("Officer not found: " + officerId));
        if (officer.getRole() != Role.COLLECTIONS_OFFICER || !branchId.equals(officer.getBranchId())) {
            throw ApiException.badRequest("Selected user is not a Collections Officer in your branch");
        }

        c.setAssignedCollectionsOfficerId(officerId);
        c.setAssignedDate(LocalDateTime.now());
        c.setStatus(CaseStatus.ASSIGNED);
        caseRepository.save(c);

        String borrowerName = borrowerNameForCase(c);
        auditService.record(managerUserId, "DELINQUENCY_CASE_ASSIGNED", "DELINQUENCY",
                "caseId=" + caseId + " officerId=" + officerId);

        notificationGateway.notifyUser(officerId, Role.COLLECTIONS_OFFICER.name(),
                "You have been assigned delinquency case #" + caseId + " for borrower " + borrowerName
                        + " (loan #" + c.getLoanAccountId() + "): DPD " + c.getDpd() + ", " + c.getParBucket(),
                NotificationCategory.ASSIGNMENT, "DelinquencyCase", caseId);

        return toResponse(c);
    }

    // ----------------------------------------------------------------
    // Collections Officer view
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DelinquencyCaseResponse> listCasesForOfficer(Long officerId) {
        return caseRepository.findByAssignedCollectionsOfficerId(officerId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private DelinquencyCaseResponse toResponse(DelinquencyCase c) {
        Long borrowerId = loanAccountRepository.findById(c.getLoanAccountId())
                .map(LoanAccount::getBorrowerId).orElse(null);
        String borrowerName = borrowerId == null ? "Unknown"
                : borrowerRepository.findById(borrowerId).map(Borrower::getName).orElse("Unknown");
        String officerName = c.getAssignedCollectionsOfficerId() == null ? null
                : userRepository.findById(c.getAssignedCollectionsOfficerId()).map(User::getName).orElse(null);
        return DelinquencyCaseResponse.from(c, borrowerId, borrowerName, officerName);
    }

    private String borrowerNameForCase(DelinquencyCase c) {
        return loanAccountRepository.findById(c.getLoanAccountId())
                .flatMap(a -> borrowerRepository.findById(a.getBorrowerId()))
                .map(Borrower::getName).orElse("Unknown");
    }

    private Long branchOfUser(Long userId) {
        return userRepository.findById(userId).map(User::getBranchId).orElse(null);
    }

    private Long branchOfLoanAccount(Long loanAccountId) {
        return loanAccountRepository.findById(loanAccountId)
                .flatMap(a -> borrowerRepository.findById(a.getBorrowerId()))
                .map(this::branchOfBorrower).orElse(null);
    }

    private Long branchOfBorrower(Borrower b) {
        return userRepository.findById(b.getRegisteredByFieldOfficerId())
                .map(User::getBranchId).orElse(null);
    }

    private Long branchManagerFor(Borrower borrower) {
        if (borrower == null) {
            return null;
        }
        Long branchId = branchOfBorrower(borrower);
        if (branchId == null) {
            return null;
        }
        return userRepository.findByBranchIdAndRole(branchId, Role.BRANCH_MANAGER).stream()
                .findFirst().map(User::getUserId).orElse(null);
    }
}
