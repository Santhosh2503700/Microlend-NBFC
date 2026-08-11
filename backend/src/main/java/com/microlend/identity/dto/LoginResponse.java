package com.microlend.identity.dto;

import com.microlend.identity.enums.Role;


public record LoginResponse(
        String token,
        boolean forcePasswordReset,
        Long userId,
        String name,
        Role role,
        Long branchId
) {
}
