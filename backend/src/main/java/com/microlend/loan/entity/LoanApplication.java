package com.microlend.loan.entity;


import com.microlend.loan.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "loan_application", indexes = {
        @Index(name = "idx_app_borrower", columnList = "borrower_id"),
        @Index(name = "idx_app_credit_officer", columnList = "credit_officer_id"),
        @Index(name = "idx_app_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    // Nullable; required only when LoanProduct.Category = GroupLending.
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "loan_product_id", nullable = false)
    private Long loanProductId;

    @Column(name = "requested_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (requested_amount > 0)")
    private BigDecimal requestedAmount;

    @Column(name = "purpose", length = 500)
    private String purpose;

    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;

    @Column(name = "credit_officer_id")
    private Long creditOfficerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @PrePersist
    void onCreate() {
        if (applicationDate == null) {
            applicationDate = LocalDateTime.now();
        }
    }
}
