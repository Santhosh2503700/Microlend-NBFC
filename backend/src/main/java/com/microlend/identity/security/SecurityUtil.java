package com.microlend.identity.security;

import com.microlend.identity.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static AppUserPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal p) {
            return p;
        }
        return null;
    }

    public static Long currentUserId() {
        AppUserPrincipal p = currentPrincipal();
        return p == null ? null : p.userId();
    }

    public static Role currentRole() {
        AppUserPrincipal p = currentPrincipal();
        return p == null ? null : p.role();
    }
}
