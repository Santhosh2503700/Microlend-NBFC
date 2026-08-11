package com.microlend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisputeRequest(
        @NotBlank @Size(max = 1000, message = "Dispute remarks cannot exceed 1000 characters") String disputeRemarks) {
}
