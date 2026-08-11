package com.microlend.analytics.dto;

import java.math.BigDecimal;

// Field Officer productivity ranking row.
public record OfficerPerformanceRow(
        Long officerId,
        String officerName,
        long borrowers,
        long groups,
        long collections,
        BigDecimal collectedAmount,
        BigDecimal collectionEfficiencyPercent
) {
}
