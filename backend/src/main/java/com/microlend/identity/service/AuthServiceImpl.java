package com.microlend.identity.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.common.ApiException;
import com.microlend.identity.dto.LoginRequest;
import com.microlend.identity.dto.LoginResponse;
import com.microlend.identity.dto.MeResponse;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import com.microlend.identity.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String MODULE = "AUTH";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditGateway auditService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            // Audit every failed login attempt (userId may be null when the email is unknown).
            auditService.record(user == null ? null : user.getUserId(),
                    "LOGIN_FAILED", MODULE, "email=" + request.email());
            throw ApiException.unauthorized("Invalid email or password");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditService.record(user.getUserId(), "LOGIN_BLOCKED_INACTIVE", MODULE, "status=" + user.getStatus());
            throw ApiException.forbidden("Account is not active");
        }

        if (user.isMustResetPassword()) {
            String resetToken = jwtService.generateResetToken(user.getUserId());
            auditService.record(user.getUserId(), "LOGIN_SUCCESS_FORCE_RESET", MODULE, null);
            return new LoginResponse(resetToken, true, user.getUserId(), user.getName(), user.getRole(), user.getBranchId());
        }

        String token = jwtService.generateFullToken(user.getUserId(), user.getRole());
        auditService.record(user.getUserId(), "LOGIN_SUCCESS", MODULE, null);
        return new LoginResponse(token, false, user.getUserId(), user.getName(), user.getRole(), user.getBranchId());
    }


    @Transactional
    public LoginResponse resetPassword(Long userId, String newPasswordRaw) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPasswordRaw));
        user.setMustResetPassword(false);
        user.setLastPasswordChangeDate(LocalDateTime.now());
        userRepository.save(user);

        auditService.record(userId, "PASSWORD_RESET_COMPLETED", MODULE, null);

        String token = jwtService.generateFullToken(user.getUserId(), user.getRole());
        return new LoginResponse(token, false, user.getUserId(), user.getName(), user.getRole(), user.getBranchId());
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        return new MeResponse(user.getUserId(), user.getName(), user.getEmail(),
                user.getRole(), user.getBranchId(), user.isMustResetPassword());
    }
}
