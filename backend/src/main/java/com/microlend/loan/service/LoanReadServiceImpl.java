package com.microlend.loan.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.service.BorrowerService;
import com.microlend.common.ApiException;
import com.microlend.loan.dto.BorrowerDashboardResponse;
import com.microlend.loan.dto.LoanAccountResponse;
import com.microlend.loan.dto.RepaymentScheduleResponse;
import com.microlend.loan.enums.ApplicationStatus;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.entity.LoanApplication;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.LoanApplicationRepository;
import com.microlend.loan.repository.LoanProductRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class LoanReadServiceImpl implements LoanReadService {

    private final LoanAccountRepository loanAccountRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final LoanApplicationRepository applicationRepository;
    private final LoanProductRepository productRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowerService borrowerService;

    // ---------------- Borrower self-service ----------------

    @Transactional(readOnly = true)
    public Borrower requireBorrowerForPortalUser(Long portalUserId) {
        return borrowerRepository.findByPortalUserId(portalUserId)
                .orElseThrow(() -> ApiException.notFound("No borrower profile linked to this account"));
    }

    @Transactional(readOnly = true)
    public BorrowerDashboardResponse dashboardForPortalUser(Long portalUserId) {
        Borrower borrower = requireBorrowerForPortalUser(portalUserId);
        Long borrowerId = borrower.getBorrowerId();

        List<LoanAccount> accounts = loanAccountRepository.findByBorrowerId(borrowerId);
        long activeLoanCount = accounts.stream()
                .filter(a -> a.getStatus() == LoanAccountStatus.ACTIVE).count();
        BigDecimal totalOutstanding = accounts.stream()
                .filter(a -> a.getStatus() == LoanAccountStatus.ACTIVE)
                .map(LoanAccount::getOutstandingPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Amount currently due = Σ totalDue of all OVERDUE installments across the borrower's loans.
        BigDecimal amountDue = BigDecimal.ZERO;
        RepaymentSchedule nextInstallment = null;
        for (LoanAccount a : accounts) {
            List<RepaymentSchedule> rows =
                    scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(a.getLoanAccountId());
            for (RepaymentSchedule s : rows) {
                if (s.getStatus() == ScheduleStatus.OVERDUE) {
                    amountDue = amountDue.add(s.getTotalDue());
                }
                if (s.getStatus() == ScheduleStatus.PENDING || s.getStatus() == ScheduleStatus.OVERDUE) {
                    if (nextInstallment == null || s.getDueDate().isBefore(nextInstallment.getDueDate())) {
                        nextInstallment = s;
                    }
                }
            }
        }

        List<LoanApplication> apps = applicationRepository.findByBorrowerId(borrowerId);
        long underAssessment = countStatus(apps, ApplicationStatus.UNDER_ASSESSMENT);
        long approved = countStatus(apps, ApplicationStatus.APPROVED);
        long sanctioned = countStatus(apps, ApplicationStatus.SANCTIONED);
        long disbursed = countStatus(apps, ApplicationStatus.DISBURSED);
        long waitlisted = countStatus(apps, ApplicationStatus.WAITLISTED);
        long rejected = countStatus(apps, ApplicationStatus.REJECTED);

        return new BorrowerDashboardResponse(
                activeLoanCount, totalOutstanding, amountDue,
                nextInstallment == null ? null : nextInstallment.getDueDate(),
                nextInstallment == null ? null : nextInstallment.getTotalDue(),
                underAssessment, approved, sanctioned, disbursed, waitlisted, rejected);
    }

    @Transactional(readOnly = true)
    public List<LoanAccountResponse> loansForPortalUser(Long portalUserId) {
        Borrower borrower = requireBorrowerForPortalUser(portalUserId);
        return loansFor(borrower);
    }

    @Transactional(readOnly = true)
    public List<RepaymentScheduleResponse> scheduleForPortalUser(Long portalUserId, Long loanAccountId) {
        Borrower borrower = requireBorrowerForPortalUser(portalUserId);
        LoanAccount account = requireAccount(loanAccountId);
        if (!account.getBorrowerId().equals(borrower.getBorrowerId())) {
            throw ApiException.forbidden("Loan account does not belong to this borrower");
        }
        return schedule(loanAccountId);
    }

    // ---------------- Field Officer loan look-ups ----------------

    @Transactional(readOnly = true)
    public List<LoanAccountResponse> loansForOfficerBorrower(Long officerId, Long borrowerId) {
        Borrower borrower = borrowerService.getOwned(officerId, borrowerId); // enforces rule 4
        return loansFor(borrower);
    }

    @Transactional(readOnly = true)
    public List<RepaymentScheduleResponse> scheduleForOfficerLoan(Long officerId, Long loanAccountId) {
        LoanAccount account = requireAccount(loanAccountId);
        borrowerService.getOwned(officerId, account.getBorrowerId()); // enforces rule 4
        return schedule(loanAccountId);
    }

    // ---------------- Shared helpers ----------------

    private List<LoanAccountResponse> loansFor(Borrower borrower) {
        String borrowerName = borrower.getName();
        return loanAccountRepository.findByBorrowerId(borrower.getBorrowerId()).stream()
                .sorted(Comparator.comparing(LoanAccount::getLoanAccountId))
                .map(a -> LoanAccountResponse.from(a, borrowerName, productName(a.getProductId())))
                .toList();
    }

    private List<RepaymentScheduleResponse> schedule(Long loanAccountId) {
        return scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(loanAccountId).stream()
                .map(RepaymentScheduleResponse::from).toList();
    }

    private LoanAccount requireAccount(Long loanAccountId) {
        return loanAccountRepository.findById(loanAccountId)
                .orElseThrow(() -> ApiException.notFound("Loan account not found: " + loanAccountId));
    }

    private String productName(Long productId) {
        return productRepository.findById(productId).map(LoanProduct::getProductName).orElse("Unknown");
    }

    private long countStatus(List<LoanApplication> apps, ApplicationStatus status) {
        return apps.stream().filter(a -> a.getStatus() == status).count();
    }
}
