package com.microlend.loan.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public record BorrowerDashboardResponse(
        long activeLoanCount,
        BigDecimal totalOutstandingPrincipal,
        BigDecimal amountCurrentlyDue,
        LocalDate nextDueDate,
        BigDecimal nextDueAmount,
        long applicationsUnderAssessment,
        long applicationsApproved,
        long applicationsSanctioned,
        long applicationsDisbursed,
        long applicationsWaitlisted,
        long applicationsRejected
) {
}
