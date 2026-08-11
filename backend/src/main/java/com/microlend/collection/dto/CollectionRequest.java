package com.microlend.collection.dto;

import com.microlend.collection.enums.CollectionMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CollectionRequest(
        @NotNull Long loanAccountId,
        @NotNull Long scheduleId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal collectedAmount,
        @NotNull CollectionMode mode,
        Long centreMeetingId
) {
}
