package com.microlend.loan.dto;

import com.microlend.loan.enums.InterestType;
import com.microlend.loan.enums.LoanCategory;
import com.microlend.loan.enums.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LoanProductRequest(
        @NotBlank @Size(max = 120, message = "Product name cannot exceed 120 characters") String productName,
        @NotNull LoanCategory category,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2, message = "Min amount is out of range") BigDecimal minAmount,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2, message = "Max amount is out of range") BigDecimal maxAmount,
        @NotNull @Min(1) @Max(600) Integer tenureMonths,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @DecimalMax(value = "100.0", message = "Interest rate cannot exceed 100%") BigDecimal interestRatePercent,
        @NotNull InterestType interestType,
        @DecimalMin(value = "0.0")
        @DecimalMax(value = "100.0", message = "Processing fee cannot exceed 100%") BigDecimal processingFeePercent,
        ProductStatus status
) {
}
