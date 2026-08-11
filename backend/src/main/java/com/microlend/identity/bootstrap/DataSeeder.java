package com.microlend.identity.bootstrap;

import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    // Default one-time password for all seed accounts (must be reset on first login).
    public static final String DEFAULT_PASSWORD = "1234";
    private static final Long DEFAULT_BRANCH_ID = 1L;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private record Seed(String name, String email, String phone, Role role, Long branchId) {
    }

    @Override
    public void run(String... args) {
        List<Seed> seeds = List.of(
                new Seed("System Administrator", "admin@microlend.com", "9361321910", Role.NBFC_ADMIN, DEFAULT_BRANCH_ID),
                new Seed("Field Officer Demo", "fieldofficer@microlend.com", "9361321911", Role.FIELD_OFFICER, DEFAULT_BRANCH_ID),
                new Seed("Credit Officer Demo", "creditofficer@microlend.com", "9361321912", Role.CREDIT_OFFICER, DEFAULT_BRANCH_ID),
                new Seed("Branch Manager Demo", "branchmanager@microlend.com", "9361321913", Role.BRANCH_MANAGER, DEFAULT_BRANCH_ID),
                new Seed("Collections Officer Demo", "collectionofficer@microlend.com", "9361321914", Role.COLLECTIONS_OFFICER, DEFAULT_BRANCH_ID),
                new Seed("Borrower Demo", "borrower@microlend.com", "9361321915", Role.BORROWER, DEFAULT_BRANCH_ID)
        );

        String hash = passwordEncoder.encode(DEFAULT_PASSWORD);
        int created = 0;
        for (Seed s : seeds) {
            if (userRepository.existsByEmail(s.email())) {
                continue;
            }
            User u = User.builder()
                    .name(s.name())
                    .email(s.email())
                    .phone(s.phone())
                    .role(s.role())
                    .branchId(s.branchId())
                    .passwordHash(hash)
                    .status(UserStatus.ACTIVE)
                    .mustResetPassword(true)
                    .build();
            userRepository.save(u);
            created++;
        }
        if (created > 0) {
            log.info("DataSeeder: created {} seed users (default password redacted, forced reset on first login).",
                    created);
        } else {
            log.info("DataSeeder: seed users already present, nothing to create.");
        }
    }
}
