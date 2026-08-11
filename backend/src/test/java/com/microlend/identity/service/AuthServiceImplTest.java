package com.microlend.identity.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.common.ApiException;
import com.microlend.identity.dto.LoginRequest;
import com.microlend.identity.dto.LoginResponse;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.Role;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import com.microlend.identity.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwtService;
    @Mock
    AuditGateway auditService;
    @InjectMocks
    AuthServiceImpl service;

    private User activeUser(boolean mustReset) {
        return User.builder().userId(1L).name("Admin").email("a@b.com").passwordHash("hash")
                .status(UserStatus.ACTIVE).mustResetPassword(mustReset).role(Role.NBFC_ADMIN).branchId(1L).build();
    }

    @Test
    void unknownEmailThrowsAndAuditsFailure() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(new LoginRequest("a@b.com", "x")))
                .isInstanceOf(ApiException.class);
        verify(auditService).record(isNull(), eq("LOGIN_FAILED"), eq("AUTH"), anyString());
    }

    @Test
    void forcedResetReturnsResetToken() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(activeUser(true)));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);
        when(jwtService.generateResetToken(1L)).thenReturn("RESET");

        LoginResponse res = service.login(new LoginRequest("a@b.com", "pw"));

        assertThat(res.forcePasswordReset()).isTrue();
        assertThat(res.token()).isEqualTo("RESET");
    }

    @Test
    void successfulLoginReturnsFullToken() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(activeUser(false)));
        when(passwordEncoder.matches("pw", "hash")).thenReturn(true);
        when(jwtService.generateFullToken(1L, Role.NBFC_ADMIN)).thenReturn("FULL");

        LoginResponse res = service.login(new LoginRequest("a@b.com", "pw"));

        assertThat(res.forcePasswordReset()).isFalse();
        assertThat(res.token()).isEqualTo("FULL");
    }
}
