package com.microlend.borrower.entity;


import com.microlend.borrower.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "credit_assessment", indexes = {
        @Index(name = "idx_assessment_borrower", columnList = "borrower_id"),
        @Index(name = "idx_assessment_application", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    //The application this assessment was computed for (enables per-application review).
    @Column(name = "application_id")
    private Long applicationId;

    // Null / SYSTEM sentinel for automatic assessments.
    @Column(name = "assessed_by_id")
    private Long assessedById;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    @Column(name = "internal_credit_score", precision = 6, scale = 2)
    private BigDecimal internalCreditScore;

    @Column(name = "debt_burden_ratio", precision = 6, scale = 4)
    private BigDecimal debtBurdenRatio;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 8)
    private Recommendation recommendation;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_type", nullable = false, length = 20)
    @Builder.Default
    private AssessmentType assessmentType = AssessmentType.AUTOMATIC;

    @Column(name = "overridden_by_id")
    private Long overriddenById;

    @Column(name = "override_remarks", length = 1000)
    private String overrideRemarks;

    // Preserved original automatic values when a manual override is applied.
    @Column(name = "original_recommendation", length = 8)
    private String originalRecommendation;

    @PrePersist
    void onCreate() {
        if (assessmentDate == null) {
            assessmentDate = LocalDateTime.now();
        }
    }
}
