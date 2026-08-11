package com.microlend.identity.dto;

import com.microlend.identity.enums.Role;
import com.microlend.identity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private UserStatus status;
    private String phone;
    private String bankAccountNumber;
    private String ifscCode;
    private Long branchId;
}