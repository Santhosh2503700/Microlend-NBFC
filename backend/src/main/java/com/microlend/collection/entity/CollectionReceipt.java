package com.microlend.collection.entity;


import com.microlend.collection.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "collection_receipt", indexes = {
        @Index(name = "idx_receipt_borrower", columnList = "borrower_id"),
        @Index(name = "idx_receipt_account", columnList = "loan_account_id"),
        @Index(name = "idx_receipt_approval", columnList = "borrower_approval_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "loan_account_id", nullable = false)
    private Long loanAccountId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "field_officer_id", nullable = false)
    private Long fieldOfficerId;

    @Column(name = "stated_amount", nullable = false, precision = 15, scale = 2,
            columnDefinition = "DECIMAL(15,2) CHECK (stated_amount > 0)")
    private BigDecimal statedAmount;

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 20)
    private CollectionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrower_approval_status", nullable = false, length = 12)
    @Builder.Default
    private BorrowerApprovalStatus borrowerApprovalStatus = BorrowerApprovalStatus.PENDING;

    @Column(name = "borrower_approved_date")
    private LocalDateTime borrowerApprovedDate;

    @Column(name = "dispute_remarks", length = 1000)
    private String disputeRemarks;

    /** Set when a manager/collections officer co-signs on the borrower's behalf. */
    @Column(name = "co_signed_by_id")
    private Long coSignedById;

    @Column(name = "co_sign_justification", length = 1000)
    private String coSignJustification;

    @Column(name = "generated_timestamp", nullable = false)
    private LocalDateTime generatedTimestamp;

    @PrePersist
    void onCreate() {
        if (generatedTimestamp == null) {
            generatedTimestamp = LocalDateTime.now();
        }
    }
}
