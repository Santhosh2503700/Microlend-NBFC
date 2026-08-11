package com.microlend.delinquency.dto;

import jakarta.validation.constraints.NotNull;

public record AssignOfficerRequest(
        @NotNull(message = "collectionsOfficerId is required")
        Long collectionsOfficerId
) {
}
