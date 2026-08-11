package com.microlend.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        @NotNull Long borrowerId,
        @NotNull Long loanProductId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2, message = "Requested amount is out of range") BigDecimal requestedAmount,
        @Size(max = 500, message = "Purpose cannot exceed 500 characters") String purpose,
        Long groupId
) {
}
