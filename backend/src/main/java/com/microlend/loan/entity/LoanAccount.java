package com.microlend.loan.entity;


import com.microlend.loan.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "loan_account", indexes = {
        @Index(name = "idx_account_borrower", columnList = "borrower_id"),
        @Index(name = "idx_account_application", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_account_id")
    private Long loanAccountId;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "disbursed_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (disbursed_amount > 0)")
    private BigDecimal disbursedAmount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    @Column(name = "total_interest", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterest;

    @Column(name = "total_repayable", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalRepayable;

    @Column(name = "outstanding_principal", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingPrincipal;

    /** Days Past Due — recomputed by the Phase 6 delinquency scheduler. */
    @Column(name = "dpd", nullable = false)
    @Builder.Default
    private Integer dpd = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    @Builder.Default
    private LoanAccountStatus status = LoanAccountStatus.ACTIVE;
}
