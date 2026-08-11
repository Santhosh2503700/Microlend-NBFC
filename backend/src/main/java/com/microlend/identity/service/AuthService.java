package com.microlend.identity.service;

import com.microlend.identity.dto.LoginRequest;
import com.microlend.identity.dto.LoginResponse;
import com.microlend.identity.dto.MeResponse;


public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse resetPassword(Long userId, String newPasswordRaw);

    MeResponse me(Long userId);
}
