package com.microlend.identity.entity;


import com.microlend.identity.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Role role;

    // Email: UNIQUE per User, standard email format CHECK
    @Email
    @Column(name = "email", nullable = false, length = 190,
            columnDefinition = "VARCHAR(190) CHECK (email LIKE '%_@_%._%')")
    private String email;

    // Mobile Number: exactly 10 numeric digits, UNIQUE per User
    @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 numeric digits")
    @Column(name = "phone", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) CHECK (phone REGEXP '^[0-9]{10}$')")
    private String phone;

    @Column(name = "branch_id")
    private Long branchId;

    /** Never stored in plaintext — BCrypt hash only. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "must_reset_password", nullable = false)
    @Builder.Default
    private boolean mustResetPassword = true;

    @Column(name = "last_password_change_date")
    private LocalDateTime lastPasswordChangeDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }
}