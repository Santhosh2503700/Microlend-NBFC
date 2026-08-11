package com.microlend.borrower.dto;

import com.microlend.borrower.enums.BorrowerType;
import com.microlend.borrower.enums.Gender;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BorrowerRegistrationRequest(
        @NotBlank
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,

        // DOB must be provided and must be a date in the past
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be a valid past date")
        LocalDate dateOfBirth,

        @NotNull Gender gender,

        @NotBlank
        @Pattern(regexp = "\\d{12}", message = "National ID must be 12 digits")
        String nationalIdNumber,

        @NotBlank
        @Size(max = 120, message = "Village cannot exceed 120 characters")
        String village,

        @NotBlank
        @Size(max = 120, message = "District cannot exceed 120 characters")
        String district,

        @NotBlank
        @Pattern(regexp = "\\d{10}", message = "Phone must be 10 digits")
        String phone,

        @Size(max = 120, message = "Occupation cannot exceed 120 characters")
        String occupation,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2, message = "Monthly income is out of range")
        BigDecimal monthlyIncome,

        @NotBlank
        @Pattern(regexp = "\\d{9,18}", message = "Bank account must be 9-18 digits")
        String bankAccountNumber,

        @NotBlank
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC")
        String ifscCode,

        @NotBlank
        @Email
        @Size(max = 120, message = "Email cannot exceed 120 characters")
        String portalEmail,

        @NotNull BorrowerType borrowerType,

        Long centreId,

        Long groupId
) {
}