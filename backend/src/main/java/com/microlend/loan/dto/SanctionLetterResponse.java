package com.microlend.loan.dto;

import com.microlend.loan.entity.SanctionLetter;
import com.microlend.loan.enums.SanctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SanctionLetterResponse(
        Long sanctionId,
        Long applicationId,
        Long borrowerId,
        String borrowerName,
        BigDecimal sanctionedAmount,
        BigDecimal interestRate,
        Integer tenure,
        BigDecimal emiAmount,
        String disbursalConditions,
        LocalDateTime issuedDate,
        boolean acceptedByBorrower,
        SanctionStatus status
) {
    public static SanctionLetterResponse from(SanctionLetter s, Long borrowerId, String borrowerName) {
        return new SanctionLetterResponse(s.getSanctionId(), s.getApplicationId(), borrowerId, borrowerName,
                s.getSanctionedAmount(), s.getInterestRate(), s.getTenure(), s.getEmiAmount(),
                s.getDisbursalConditions(), s.getIssuedDate(), s.isAcceptedByBorrower(), s.getStatus());
    }
}
