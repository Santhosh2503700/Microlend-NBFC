package com.microlend.grouporigination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

// BranchID and CreatedByFieldOfficerID are NEVER accepted here — resolved from the session.
public record CentreRequest(
        @NotBlank @Size(max = 120, message = "Centre name cannot exceed 120 characters") String centreName,
        @NotBlank @Size(max = 120, message = "Village cannot exceed 120 characters") String village,
        @Size(max = 12, message = "Meeting day cannot exceed 12 characters") String meetingDay,
        LocalTime meetingTime
) {
}
