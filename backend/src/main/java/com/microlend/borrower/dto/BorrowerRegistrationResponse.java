package com.microlend.borrower.dto;


public record BorrowerRegistrationResponse(
        BorrowerResponse borrower,
        Long portalUserId,
        String portalEmail,
        String portalDefaultPassword,
        String message
) {
}
