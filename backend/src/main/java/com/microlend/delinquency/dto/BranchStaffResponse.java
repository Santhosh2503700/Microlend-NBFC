package com.microlend.delinquency.dto;

import com.microlend.identity.entity.User;

public record BranchStaffResponse(
        Long userId,
        String name,
        String email,
        String role
) {
    public static BranchStaffResponse from(User u) {
        return new BranchStaffResponse(u.getUserId(), u.getName(), u.getEmail(), u.getRole().name());
    }
}
