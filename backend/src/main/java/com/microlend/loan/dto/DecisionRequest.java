package com.microlend.loan.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;


public record DecisionRequest(
        @NotNull Action action,
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2, message = "Sanctioned amount is out of range") BigDecimal sanctionedAmount,
        boolean override,
        @Size(max = 1000, message = "Override remarks cannot exceed 1000 characters") String overrideRemarks
) {
    public enum Action {
        APPROVE, WAITLIST, REJECT
    }
}
