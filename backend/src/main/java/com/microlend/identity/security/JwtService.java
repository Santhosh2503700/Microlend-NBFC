package com.microlend.identity.security;

import com.microlend.identity.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {

    public static final String SCOPE_RESET = "RESET";
    public static final String SCOPE_FULL = "FULL";

    private final SecretKey key;
    private final long fullTtlMs;
    private final long resetTtlMs;

    public JwtService(
            @Value("${microlend.security.jwt.secret:microlend-dev-secret-key-change-me-in-prod-0123456789}") String secret,
            @Value("${microlend.security.jwt.full-ttl-minutes:480}") long fullTtlMinutes,
            @Value("${microlend.security.jwt.reset-ttl-minutes:15}") long resetTtlMinutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.fullTtlMs = fullTtlMinutes * 60_000;
        this.resetTtlMs = resetTtlMinutes * 60_000;
    }

    public String generateFullToken(Long userId, Role role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("role", role.name(), "scope", SCOPE_FULL))
                .issuedAt(new Date(now))
                .expiration(new Date(now + fullTtlMs))
                .signWith(key)
                .compact();
    }

    // Short-lived, reset-only token issued when {@code mustResetPassword == true}.
    public String generateResetToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(Map.of("scope", SCOPE_RESET))
                .issuedAt(new Date(now))
                .expiration(new Date(now + resetTtlMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
