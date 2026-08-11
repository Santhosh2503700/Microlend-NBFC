package com.microlend.loan.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.entity.CreditAssessment;
import com.microlend.borrower.enums.KycStatus;
import com.microlend.borrower.enums.Recommendation;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.repository.CreditAssessmentRepository;
import com.microlend.borrower.service.CreditAssessmentService;
import com.microlend.common.ApiException;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import com.microlend.loan.dto.*;
import com.microlend.loan.entity.*;
import com.microlend.loan.enums.*;
import com.microlend.loan.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class LoanApplicationServiceImpl implements LoanApplicationService {

    private final LoanApplicationRepository applicationRepository;
    private final LoanProductRepository productRepository;
    private final SanctionLetterRepository sanctionLetterRepository;
    private final BorrowerRepository borrowerRepository;
    private final BorrowerKYCRepository kycRepository;
    private final CreditAssessmentRepository assessmentRepository;
    private final CreditAssessmentService assessmentService;
    private final EmiCalculationService emiService;
    private final UserRepository userRepository;
    private final AuditGateway auditService;

    // ---------------- 3.5 Submission ----------------

    @Transactional
    public LoanApplicationResponse submit(Long currentUserId, Role role, LoanApplicationRequest req) {
        Borrower borrower = borrowerRepository.findById(req.borrowerId())
                .orElseThrow(() -> ApiException.notFound("Borrower not found: " + req.borrowerId()));

        // Ownership / self-service checks.
        if (role == Role.FIELD_OFFICER) {
            if (!borrower.getRegisteredByFieldOfficerId().equals(currentUserId)) {
                throw ApiException.forbidden("Borrower not registered by this officer");
            }
        } else if (role == Role.BORROWER) {
            if (!currentUserId.equals(borrower.getPortalUserId())) {
                throw ApiException.forbidden("Borrowers may only apply for themselves");
            }
        } else {
            throw ApiException.forbidden("Only a Field Officer or the Borrower may submit an application");
        }

        LoanProduct product = productRepository.findById(req.loanProductId())
                .orElseThrow(() -> ApiException.notFound("Loan product not found: " + req.loanProductId()));

        // Amount within product band.
        if (req.requestedAmount().compareTo(product.getMinAmount()) < 0
                || req.requestedAmount().compareTo(product.getMaxAmount()) > 0) {
            throw ApiException.badRequest("Requested amount must be between " + product.getMinAmount()
                    + " and " + product.getMaxAmount());
        }

        // GroupLending group rule.
        Long groupId = req.groupId();
        if (product.getCategory() == LoanCategory.GROUP_LENDING) {
            if (groupId == null) {
                groupId = borrower.getGroupId();
            }
            if (groupId == null) {
                throw ApiException.badRequest("GroupLending product requires a group");
            }
            if (borrower.getGroupId() == null || !groupId.equals(borrower.getGroupId())) {
                throw ApiException.forbidden("Group does not match the borrower's registered group");
            }
        } else if (groupId != null) {
            throw ApiException.badRequest("Non-group product must not carry a groupId");
        }

        // KYC: at least one Verified document required.
        if (kycRepository.countByBorrowerIdAndStatus(req.borrowerId(), KycStatus.VERIFIED) < 1) {
            throw ApiException.badRequest("Borrower must have at least one Verified KYC document before applying");
        }

        LoanApplication application = LoanApplication.builder()
                .borrowerId(req.borrowerId())
                .groupId(groupId)
                .loanProductId(req.loanProductId())
                .requestedAmount(req.requestedAmount())
                .purpose(req.purpose())
                .applicationDate(LocalDateTime.now())
                .status(ApplicationStatus.UNDER_ASSESSMENT)
                .build();
        application = applicationRepository.save(application);

        // automatic assessment runs immediately (no manual score entry, no blank form).
        assessmentService.assess(application.getApplicationId(), req.borrowerId(), product, req.requestedAmount());

        auditService.record(currentUserId, "LOAN_APPLICATION_SUBMITTED", "LOAN_APPLICATION",
                "applicationId=" + application.getApplicationId() + " borrowerId=" + req.borrowerId());

        return toResponse(application, borrower, product);
    }

    // ---------------- Queries ----------------

    @Transactional(readOnly = true)
    public List<LoanApplicationResponse> listForRole(Long currentUserId, Role role, Long branchId) {
        List<LoanApplication> apps;
        switch (role) {
            case FIELD_OFFICER -> {
                List<Long> borrowerIds = borrowerRepository.findByRegisteredByFieldOfficerId(currentUserId)
                        .stream().map(Borrower::getBorrowerId).toList();
                apps = borrowerIds.isEmpty() ? List.of() : applicationRepository.findByBorrowerIdIn(borrowerIds);
            }
            case BORROWER -> {
                Borrower b = borrowerRepository.findByPortalUserId(currentUserId).orElse(null);
                apps = b == null ? List.of() : applicationRepository.findByBorrowerId(b.getBorrowerId());
            }
            case CREDIT_OFFICER, BRANCH_MANAGER, NBFC_ADMIN -> apps = applicationRepository.findAll();
            default -> apps = List.of();
        }
        return apps.stream().map(this::toResponseResolved).toList();
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(Long applicationId) {
        CreditAssessment a = assessmentRepository
                .findFirstByApplicationIdOrderByAssessmentDateDesc(applicationId)
                .orElseThrow(() -> ApiException.notFound("No assessment found for application " + applicationId));
        return AssessmentResponse.from(a);
    }

    // ---------------- Decision → automatic sanction ----------------

    @Transactional
    public Map<String, Object> decide(Long creditOfficerId, Long applicationId, DecisionRequest req) {
        LoanApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> ApiException.notFound("Application not found: " + applicationId));

        if (application.getStatus() == ApplicationStatus.SANCTIONED
                || application.getStatus() == ApplicationStatus.DISBURSED
                || application.getStatus() == ApplicationStatus.REJECTED) {
            throw ApiException.badRequest("Application already finalised (" + application.getStatus() + ")");
        }

        CreditAssessment latest = assessmentRepository
                .findFirstByApplicationIdOrderByAssessmentDateDesc(applicationId)
                .orElseThrow(() -> ApiException.badRequest("Application has no assessment yet"));

        application.setCreditOfficerId(creditOfficerId);

        // Override: preserve the original automatic values, record a new MANUAL_OVERRIDE row.
        if (req.override()) {
            if (req.overrideRemarks() == null || req.overrideRemarks().isBlank()) {
                throw ApiException.badRequest("Override requires overrideRemarks");
            }
            Recommendation impliedByAction = switch (req.action()) {
                case APPROVE -> Recommendation.GREEN;
                case WAITLIST -> Recommendation.AMBER;
                case REJECT -> Recommendation.RED;
            };
            CreditAssessment override = CreditAssessment.builder()
                    .borrowerId(application.getBorrowerId())
                    .applicationId(applicationId)
                    .assessedById(creditOfficerId)
                    .internalCreditScore(latest.getInternalCreditScore())
                    .debtBurdenRatio(latest.getDebtBurdenRatio())
                    .recommendation(impliedByAction)
                    .remarks(latest.getRemarks())
                    .assessmentType(com.microlend.borrower.enums.AssessmentType.MANUAL_OVERRIDE)
                    .overriddenById(creditOfficerId)
                    .overrideRemarks(req.overrideRemarks())
                    .originalRecommendation(latest.getRecommendation().name())
                    .build();
            assessmentRepository.save(override);
        }

        return switch (req.action()) {
            case WAITLIST -> {
                application.setStatus(ApplicationStatus.WAITLISTED);
                applicationRepository.save(application);
                auditService.record(creditOfficerId, "LOAN_APPLICATION_WAITLISTED", "LOAN_APPLICATION",
                        "applicationId=" + applicationId);
                yield Map.of("applicationId", applicationId, "status", application.getStatus().name());
            }
            case REJECT -> {
                application.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(application);
                auditService.record(creditOfficerId, "LOAN_APPLICATION_REJECTED", "LOAN_APPLICATION",
                        "applicationId=" + applicationId);
                yield Map.of("applicationId", applicationId, "status", application.getStatus().name());
            }
            case APPROVE -> {
                SanctionLetter letter = approveAndGenerateSanction(creditOfficerId, application, req.sanctionedAmount());
                Borrower borrower = borrowerRepository.findById(application.getBorrowerId()).orElseThrow();
                yield Map.of(
                        "applicationId", applicationId,
                        "status", application.getStatus().name(),
                        "sanctionLetter", SanctionLetterResponse.from(letter,
                                borrower.getBorrowerId(), borrower.getName()));
            }
        };
    }

    private SanctionLetter approveAndGenerateSanction(Long creditOfficerId, LoanApplication application,
                                                      BigDecimal sanctionedAmountOverride) {
        LoanProduct product = productRepository.findById(application.getLoanProductId()).orElseThrow();

        BigDecimal sanctionedAmount = sanctionedAmountOverride != null
                ? sanctionedAmountOverride : application.getRequestedAmount();
        if (sanctionedAmount.signum() <= 0) {
            throw ApiException.badRequest("Sanctioned amount must be > 0");
        }
        if (sanctionedAmount.compareTo(product.getMinAmount()) < 0
                || sanctionedAmount.compareTo(product.getMaxAmount()) > 0) {
            throw ApiException.badRequest("Sanctioned amount must be within the product band");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        BigDecimal emi = emiService.calculate(sanctionedAmount, product.getInterestRatePercent(),
                product.getTenureMonths(), product.getInterestType());

        SanctionLetter letter = SanctionLetter.builder()
                .applicationId(application.getApplicationId())
                .sanctionedAmount(sanctionedAmount)
                .interestRate(product.getInterestRatePercent())
                .tenure(product.getTenureMonths())
                .emiAmount(emi)
                .disbursalConditions(disbursalConditions(product.getCategory()))
                .issuedDate(LocalDateTime.now())
                .acceptedByBorrower(false)
                .status(SanctionStatus.ISSUED)
                .build();
        letter = sanctionLetterRepository.save(letter);

        application.setStatus(ApplicationStatus.SANCTIONED);
        applicationRepository.save(application);

        auditService.record(creditOfficerId, "LOAN_APPLICATION_APPROVED", "LOAN_APPLICATION",
                "applicationId=" + application.getApplicationId()
                        + " Status: UNDER_ASSESSMENT -> APPROVED -> SANCTIONED; sanctioned=" + sanctionedAmount
                        + " EMI=" + emi);
        return letter;
    }

    private String disbursalConditions(LoanCategory category) {
        return switch (category) {
            case GROUP_LENDING -> "Group joint-liability applies. Weekly/fortnightly centre-meeting repayment. "
                    + "All group members must be current on dues.";
            case AGRICULTURE -> "Seasonal repayment aligned to harvest cycle. Proof of cultivable land required.";
            case MICRO_ENTERPRISE -> "Business proof required. Funds to be used for stated enterprise purpose.";
            case EDUCATION -> "Admission/fee proof required. Disbursed directly against institution invoice.";
            case HOUSING -> "Property/repair proof required. Staged disbursement per work progress.";
            case INDIVIDUAL -> "Standard individual loan terms. Monthly EMI as per schedule.";
        };
    }

    // ---------------- Mapping helpers ----------------

    private LoanApplicationResponse toResponseResolved(LoanApplication a) {
        String borrowerName = borrowerRepository.findById(a.getBorrowerId())
                .map(Borrower::getName).orElse("Unknown");
        String productName = productRepository.findById(a.getLoanProductId())
                .map(LoanProduct::getProductName).orElse("Unknown");
        return LoanApplicationResponse.from(a, borrowerName, productName);
    }

    private LoanApplicationResponse toResponse(LoanApplication a, Borrower b, LoanProduct p) {
        return LoanApplicationResponse.from(a, b.getName(), p.getProductName());
    }
}
