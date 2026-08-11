package com.microlend.identity.security;

import com.microlend.identity.enums.Role;

public record AppUserPrincipal(Long userId, Role role, String scope) {
}
