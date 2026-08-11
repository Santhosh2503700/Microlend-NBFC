package com.microlend.borrower.service;

import com.microlend.borrower.entity.*;
import com.microlend.borrower.enums.*;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.borrower.repository.CreditAssessmentRepository;
import com.microlend.common.ApiException;
import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.entity.RepaymentSchedule;
import com.microlend.loan.enums.ScheduleStatus;
import com.microlend.loan.repository.LoanAccountRepository;
import com.microlend.loan.repository.RepaymentScheduleRepository;
import com.microlend.loan.repository.SanctionLetterRepository;
import com.microlend.loan.service.EmiCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CreditAssessmentServiceImpl implements CreditAssessmentService {

    private final CreditPolicyProperties policy;
    private final EmiCalculationService emiService;
    private final BorrowerRepository borrowerRepository;
    private final BorrowerKYCRepository kycRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final SanctionLetterRepository sanctionLetterRepository;
    private final RepaymentScheduleRepository scheduleRepository;
    private final CreditAssessmentRepository assessmentRepository;

    // Runs automatically at application submission. AssessedByID stays null (= SYSTEM).
    @Transactional
    public CreditAssessment assess(Long applicationId, Long borrowerId, LoanProduct product,
                                   BigDecimal requestedAmount) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> ApiException.notFound("Borrower not found: " + borrowerId));
        BigDecimal income = borrower.getMonthlyIncome();

        BigDecimal prospectiveEmi = emiService.calculate(requestedAmount,
                product.getInterestRatePercent(), product.getTenureMonths(), product.getInterestType());

        // Existing active exposure (sum of EMIs on active loan accounts).
        List<LoanAccount> activeLoans =
                loanAccountRepository.findByBorrowerIdAndStatus(borrowerId, LoanAccountStatus.ACTIVE);
        BigDecimal existingEmi = BigDecimal.ZERO;
        for (LoanAccount la : activeLoans) {
            BigDecimal emi = sanctionLetterRepository.findByApplicationId(la.getApplicationId())
                    .map(sl -> sl.getEmiAmount()).orElse(BigDecimal.ZERO);
            existingEmi = existingEmi.add(emi);
        }

        // ---- DBR ----
        BigDecimal totalEmi = existingEmi.add(prospectiveEmi);
        BigDecimal dbr = income.signum() == 0 ? BigDecimal.ONE
                : totalEmi.divide(income, 4, RoundingMode.HALF_UP);

        // ---- Score components ----
        double affordability = affordabilityScore(prospectiveEmi, income);
        double exposure = exposureScore(activeLoans.size());
        double history = historyScore(activeLoans);
        double kyc = kycScore(borrowerId);
        BigDecimal score = BigDecimal.valueOf(affordability + exposure + history + kyc)
                .setScale(2, RoundingMode.HALF_UP);

        Recommendation recommendation = bucket(score, dbr);

        String remarks = String.format(
                "Auto-assessment: affordability=%.1f, exposure=%.1f, history=%.1f, kyc=%.1f; "
                        + "prospectiveEMI=%.2f, existingEMI=%.2f, DBR=%.4f",
                affordability, exposure, history, kyc, prospectiveEmi, existingEmi, dbr);

        CreditAssessment assessment = CreditAssessment.builder()
                .borrowerId(borrowerId)
                .applicationId(applicationId)
                .assessedById(null)                       // SYSTEM
                .internalCreditScore(score)
                .debtBurdenRatio(dbr)
                .recommendation(recommendation)
                .remarks(remarks)
                .assessmentType(AssessmentType.AUTOMATIC)
                .build();
        return assessmentRepository.save(assessment);
    }

    private double affordabilityScore(BigDecimal emi, BigDecimal income) {
        int w = policy.getAffordabilityWeight();
        if (income.signum() <= 0) {
            return 0;
        }
        double ratio = emi.divide(income, 6, RoundingMode.HALF_UP).doubleValue();
        double low = policy.getAffordabilityLowRatio().doubleValue();
        double high = policy.getAffordabilityHighRatio().doubleValue();
        if (ratio <= low) {
            return w;
        }
        if (ratio >= high) {
            return 0;
        }
        double frac = (high - ratio) / (high - low);
        return w * frac;
    }

    private double exposureScore(int activeLoanCount) {
        int w = policy.getExposureWeight();
        // Full marks with no active loans; -50% per active loan, floored at 0.
        double factor = Math.max(0.0, 1.0 - 0.5 * activeLoanCount);
        return w * factor;
    }

    private double historyScore(List<LoanAccount> activeLoans) {
        int w = policy.getHistoryWeight();
        int paid = 0;
        int overdue = 0;
        for (LoanAccount la : activeLoans) {
            List<RepaymentSchedule> rows =
                    scheduleRepository.findByLoanAccountIdOrderByInstallmentNumberAsc(la.getLoanAccountId());
            for (RepaymentSchedule r : rows) {
                if (r.getStatus() == ScheduleStatus.PAID) {
                    paid++;
                } else if (r.getStatus() == ScheduleStatus.OVERDUE) {
                    overdue++;
                }
            }
        }
        double onTimeRatio = (paid + overdue) == 0 ? 0.7 : (double) paid / (paid + overdue);
        return w * onTimeRatio;
    }

    private double kycScore(Long borrowerId) {
        int w = policy.getKycWeight();
        long verified = kycRepository.countByBorrowerIdAndStatus(borrowerId, KycStatus.VERIFIED);
        if (verified <= 0) {
            return 0;
        }
        double factor = Math.min(1.0, 0.8 + 0.1 * (verified - 1));
        return w * factor;
    }

    private Recommendation bucket(BigDecimal score, BigDecimal dbr) {
        if (score.compareTo(policy.getGreenMinScore()) >= 0 && dbr.compareTo(policy.getMaxDbrGreen()) <= 0) {
            return Recommendation.GREEN;
        }
        if (score.compareTo(policy.getAmberMinScore()) >= 0 && dbr.compareTo(policy.getMaxDbrAmber()) <= 0) {
            return Recommendation.AMBER;
        }
        return Recommendation.RED;
    }
}
