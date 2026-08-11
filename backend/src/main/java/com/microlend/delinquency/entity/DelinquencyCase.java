package com.microlend.delinquency.entity;


import com.microlend.delinquency.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "delinquency_case", indexes = {
        @Index(name = "idx_case_account", columnList = "loan_account_id"),
        @Index(name = "idx_case_officer", columnList = "assigned_collections_officer_id"),
        @Index(name = "idx_case_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelinquencyCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "loan_account_id", nullable = false)
    private Long loanAccountId;

    @Column(name = "dpd", nullable = false)
    private Integer dpd;

    @Enumerated(EnumType.STRING)
    @Column(name = "par_bucket", nullable = false, length = 10)
    private ParBucket parBucket;

    // Nullable — set only when a Branch Manager assigns a Collections Officer.
    @Column(name = "assigned_collections_officer_id")
    private Long assignedCollectionsOfficerId;

    @Column(name = "opened_date", nullable = false)
    private LocalDateTime openedDate;

    @Column(name = "action", length = 500)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private CaseStatus status = CaseStatus.OPEN;

    @Column(name = "notified_branch_manager_id")
    private Long notifiedBranchManagerId;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @PrePersist
    void onCreate() {
        if (openedDate == null) {
            openedDate = LocalDateTime.now();
        }
    }
}
