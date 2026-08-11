package com.microlend.loan.dto;

import java.math.BigDecimal;


public record EmiPreviewResponse(
        Long productId,
        BigDecimal minAmount, BigDecimal emiAtMin,
        BigDecimal midAmount, BigDecimal emiAtMid,
        BigDecimal maxAmount, BigDecimal emiAtMax,
        Integer tenureMonths,
        BigDecimal interestRatePercent,
        String interestType,
        String note
) {
}
