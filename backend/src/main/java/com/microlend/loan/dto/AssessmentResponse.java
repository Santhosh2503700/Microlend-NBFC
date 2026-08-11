package com.microlend.loan.dto;

import com.microlend.borrower.enums.AssessmentType;
import com.microlend.borrower.entity.CreditAssessment;
import com.microlend.borrower.enums.Recommendation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentResponse(
        Long assessmentId,
        Long applicationId,
        Long borrowerId,
        Long assessedById,          // null == SYSTEM
        LocalDateTime assessmentDate,
        BigDecimal internalCreditScore,
        BigDecimal debtBurdenRatio,
        Recommendation recommendation,
        String remarks,
        AssessmentType assessmentType,
        Long overriddenById,
        String overrideRemarks,
        String originalRecommendation
) {
    public static AssessmentResponse from(CreditAssessment a) {
        return new AssessmentResponse(a.getAssessmentId(), a.getApplicationId(), a.getBorrowerId(),
                a.getAssessedById(), a.getAssessmentDate(), a.getInternalCreditScore(),
                a.getDebtBurdenRatio(), a.getRecommendation(), a.getRemarks(), a.getAssessmentType(),
                a.getOverriddenById(), a.getOverrideRemarks(), a.getOriginalRecommendation());
    }
}
