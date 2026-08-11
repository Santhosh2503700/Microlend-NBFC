package com.microlend.loan.entity;


import com.microlend.loan.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name = "loan_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 24)
    private LoanCategory category;

    @Column(name = "min_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (min_amount > 0)")
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (max_amount > 0)")
    private BigDecimal maxAmount;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "interest_rate_percent", nullable = false, precision = 6, scale = 3,
            columnDefinition = "DECIMAL(6,3) CHECK (interest_rate_percent > 0)")
    private BigDecimal interestRatePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 20)
    private InterestType interestType;

    @Column(name = "processing_fee_percent", precision = 6, scale = 3)
    private BigDecimal processingFeePercent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
}
