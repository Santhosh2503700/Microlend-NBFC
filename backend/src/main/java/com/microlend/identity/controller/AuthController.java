package com.microlend.identity.controller;

import com.microlend.common.ApiException;
import com.microlend.identity.dto.LoginRequest;
import com.microlend.identity.dto.LoginResponse;
import com.microlend.identity.dto.MeResponse;
import com.microlend.identity.dto.ResetPasswordRequest;
import com.microlend.identity.security.AppUserPrincipal;
import com.microlend.identity.security.JwtService;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.identity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reset-password")
    public LoginResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AppUserPrincipal principal = SecurityUtil.currentPrincipal();
        if (principal == null) {
            throw ApiException.unauthorized("A valid reset token is required");
        }
        // Accept both a RESET token (first-login flow) and a FULL token (voluntary change).
        if (!JwtService.SCOPE_RESET.equals(principal.scope()) && !JwtService.SCOPE_FULL.equals(principal.scope())) {
            throw ApiException.forbidden("Invalid token scope for password reset");
        }
        return authService.resetPassword(principal.userId(), request.newPassword());
    }

    @GetMapping("/me")
    public MeResponse me() {
        AppUserPrincipal principal = SecurityUtil.currentPrincipal();
        if (principal == null) {
            throw ApiException.unauthorized("Not authenticated");
        }
        return authService.me(principal.userId());
    }
}
