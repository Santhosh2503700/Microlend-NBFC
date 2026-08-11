package com.microlend.grouporigination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;


public record GroupRequest(
        @NotBlank @Size(max = 120, message = "Group name cannot exceed 120 characters") String groupName,
        @NotNull Long centreId,
        Boolean jointLiabilityEnabled,
        List<Long> memberBorrowerIds
) {
}
