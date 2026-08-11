package com.microlend.identity.dto;

import com.microlend.identity.enums.Role;

public record MeResponse(
        Long userId,
        String name,
        String email,
        Role role,
        Long branchId,
        boolean mustResetPassword
) {
}
