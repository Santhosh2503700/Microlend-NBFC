package com.microlend.collection.entity;


import com.microlend.collection.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "collection_record", indexes = {
        @Index(name = "idx_collection_account", columnList = "loan_account_id"),
        @Index(name = "idx_collection_schedule", columnList = "schedule_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collection_id")
    private Long collectionId;

    @Column(name = "loan_account_id", nullable = false)
    private Long loanAccountId;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "collected_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (collected_amount > 0)")
    private BigDecimal collectedAmount;

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;

    @Column(name = "collected_by_id", nullable = false)
    private Long collectedById;

    /** Optional link to the centre meeting when collected via a group meeting (Phase 8b). */
    @Column(name = "centre_meeting_id")
    private Long centreMeetingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private CollectionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    @Builder.Default
    private CollectionStatus status = CollectionStatus.PENDING;
}
