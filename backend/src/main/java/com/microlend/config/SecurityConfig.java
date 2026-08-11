package com.microlend.config;

import com.microlend.identity.enums.Role;
import com.microlend.identity.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Public / Infra
                        .requestMatchers(
                                "/ping",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Authentication APIs
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/reset-password", "/api/auth/me").permitAll()

                        // Static Files
                        .requestMatchers("/uploads/**").permitAll()

                        // Receipt Co-sign
                        .requestMatchers("/api/branch-manager/receipts/*/co-sign")
                        .hasAnyRole(
                                Role.BRANCH_MANAGER.name(),
                                Role.COLLECTIONS_OFFICER.name()
                        )

                        // Role-based Access
                        .requestMatchers("/api/borrower/**")
                        .hasRole(Role.BORROWER.name())

                        .requestMatchers("/api/field-officer/**")
                        .hasRole(Role.FIELD_OFFICER.name())

                        .requestMatchers("/api/credit-officer/**")
                        .hasRole(Role.CREDIT_OFFICER.name())

                        .requestMatchers("/api/branch-manager/**")
                        .hasRole(Role.BRANCH_MANAGER.name())

                        .requestMatchers("/api/collections/**")
                        .hasRole(Role.COLLECTIONS_OFFICER.name())

                        .requestMatchers("/api/collections-officer/**")
                        .hasRole(Role.COLLECTIONS_OFFICER.name())

                        .requestMatchers("/api/admin/**")
                        .hasRole(Role.NBFC_ADMIN.name())

                        // Any authenticated API
                        .requestMatchers("/api/**")
                        .authenticated()

                        .anyRequest().permitAll()
                )

                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "Unauthorized"))
                        .accessDeniedHandler((request, response, e) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "Forbidden"))
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}