package com.microlend.collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoSignRequest(
        @NotBlank @Size(max = 1000, message = "Justification cannot exceed 1000 characters") String justification) {
}
