package com.microlend.identity.dto;

import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.UserStatus;


public record AdminUserResponse(
        Long userId,
        String name,
        String email,
        Role role,
        Long branchId,
        UserStatus status,
        boolean mustResetPassword
) {
    public static AdminUserResponse from(User u) {
        return new AdminUserResponse(u.getUserId(), u.getName(), u.getEmail(), u.getRole(),
                u.getBranchId(), u.getStatus(), u.isMustResetPassword());
    }
}
