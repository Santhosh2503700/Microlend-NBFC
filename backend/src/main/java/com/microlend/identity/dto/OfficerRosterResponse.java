package com.microlend.identity.dto;

import com.microlend.identity.enums.Role;
import com.microlend.identity.enums.UserStatus;


public record OfficerRosterResponse(
        Long userId,
        String name,
        String email,
        Role role,
        Long branchId,
        UserStatus status,
        long workloadCount,
        String workloadLabel
) {
}
