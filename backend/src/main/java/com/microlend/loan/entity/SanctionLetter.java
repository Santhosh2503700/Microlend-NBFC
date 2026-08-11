package com.microlend.loan.entity;


import com.microlend.loan.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "sanction_letter", indexes = {
        @Index(name = "idx_sanction_application", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanctionLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sanction_id")
    private Long sanctionId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "sanctioned_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (sanctioned_amount > 0)")
    private BigDecimal sanctionedAmount;

    @Column(name = "interest_rate", nullable = false, precision = 6, scale = 3)
    private BigDecimal interestRate;

    @Column(name = "tenure", nullable = false)
    private Integer tenure;

    @Column(name = "emi_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (emi_amount > 0)")
    private BigDecimal emiAmount;

    @Column(name = "disbursal_conditions", length = 1000)
    private String disbursalConditions;

    @Column(name = "issued_date", nullable = false)
    private LocalDateTime issuedDate;

    @Column(name = "accepted_by_borrower", nullable = false)
    @Builder.Default
    private boolean acceptedByBorrower = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    @Builder.Default
    private SanctionStatus status = SanctionStatus.ISSUED;

    @PrePersist
    void onCreate() {
        if (issuedDate == null) {
            issuedDate = LocalDateTime.now();
        }
    }
}
