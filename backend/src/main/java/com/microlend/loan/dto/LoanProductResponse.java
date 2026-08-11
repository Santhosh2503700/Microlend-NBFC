package com.microlend.loan.dto;

import com.microlend.loan.enums.InterestType;
import com.microlend.loan.enums.LoanCategory;
import com.microlend.loan.entity.LoanProduct;
import com.microlend.loan.enums.ProductStatus;

import java.math.BigDecimal;

public record LoanProductResponse(
        Long productId,
        String productName,
        LoanCategory category,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Integer tenureMonths,
        BigDecimal interestRatePercent,
        InterestType interestType,
        BigDecimal processingFeePercent,
        ProductStatus status
) {
    public static LoanProductResponse from(LoanProduct p) {
        return new LoanProductResponse(p.getProductId(), p.getProductName(), p.getCategory(),
                p.getMinAmount(), p.getMaxAmount(), p.getTenureMonths(), p.getInterestRatePercent(),
                p.getInterestType(), p.getProcessingFeePercent(), p.getStatus());
    }
}
