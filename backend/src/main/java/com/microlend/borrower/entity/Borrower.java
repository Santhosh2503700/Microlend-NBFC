package com.microlend.borrower.entity;


import com.microlend.borrower.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "borrower",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_borrower_national_id", columnNames = "national_id_number"),
                @UniqueConstraint(name = "uk_borrower_phone", columnNames = "phone")
        },
        indexes = {
                @Index(name = "idx_borrower_officer", columnList = "registered_by_field_officer_id"),
                @Index(name = "idx_borrower_centre", columnList = "centre_id"),
                @Index(name = "idx_borrower_group", columnList = "group_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "borrower_id")
    private Long borrowerId;

    @Size(max = 50, message = "Name cannot exceed 50 characters")
    @Column(name = "name", nullable = false, length = 50)
    private String name;


    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    // Aadhaar/NationalIDNumber: exactly 12 digits, UNIQUE across Borrowers, digit-only.
    // -- TODO Phase 3: Verhoeff checksum validation enforced at the API layer.
    @Pattern(regexp = "\\d{12}", message = "National ID (Aadhaar) must be exactly 12 digits")
    @Column(name = "national_id_number", nullable = false, length = 12,
            columnDefinition = "VARCHAR(12) CHECK (national_id_number REGEXP '^[0-9]{12}$')")
    private String nationalIdNumber;

    @Column(name = "village", nullable = false, length = 120)
    private String village;

    @Column(name = "district", nullable = false, length = 120)
    private String district;

    // Mobile Number: exactly 10 numeric digits, UNIQUE per Borrower.
    @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 numeric digits")
    @Column(name = "phone", nullable = false, length = 10,
            columnDefinition = "VARCHAR(10) CHECK (phone REGEXP '^[0-9]{10}$')")
    private String phone;

    @Column(name = "occupation", length = 120)
    private String occupation;

    // Monetary field: CHECK (monthly_income > 0)
    @Column(name = "monthly_income", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (monthly_income > 0)")
    private BigDecimal monthlyIncome;

    // BankAccountNumber: numeric, length 9–18 digits.
    @Pattern(regexp = "\\d{9,18}", message = "Bank account number must be 9–18 numeric digits")
    @Column(name = "bank_account_number", nullable = false, length = 18,
            columnDefinition = "VARCHAR(18) CHECK (bank_account_number REGEXP '^[0-9]{9,18}$')")
    private String bankAccountNumber;

    // IFSCCode: required alongside bank account, standard IFSC format (4 letters, 0, 6 alnum).
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    @Column(name = "ifsc_code", nullable = false, length = 11,
            columnDefinition = "VARCHAR(11) CHECK (ifsc_code REGEXP '^[A-Z]{4}0[A-Z0-9]{6}$')")
    private String ifscCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private BorrowerStatus status = BorrowerStatus.ACTIVE;

    @Column(name = "registered_by_field_officer_id", nullable = false)
    private Long registeredByFieldOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrower_type", nullable = false, length = 12)
    private BorrowerType borrowerType;

    // CentreID is NOT nullable — required for every borrower.
    @Column(name = "centre_id", nullable = false)
    private Long centreId;

    // GroupID is nullable — required only when BorrowerType = Group.
    @Column(name = "group_id")
    private Long groupId;

    // PortalUserID — FK to User, auto-created at registration.
    @Column(name = "portal_user_id")
    private Long portalUserId;
}
