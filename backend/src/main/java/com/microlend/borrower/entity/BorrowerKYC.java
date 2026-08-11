package com.microlend.borrower.entity;

import com.microlend.borrower.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "borrower_kyc", indexes = {
        @Index(name = "idx_kyc_borrower", columnList = "borrower_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowerKYC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kyc_id")
    private Long kycId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 24)
    private DocumentType documentType;

    @Column(name = "document_file_url", length = 500)
    private String documentFileUrl;

    @Column(name = "uploaded_by_field_officer_id", nullable = false)
    private Long uploadedByFieldOfficerId;

    @Column(name = "verified_by_id")
    private Long verifiedById;

    @Column(name = "verification_date")
    private LocalDateTime verificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private KycStatus status = KycStatus.PENDING;

    @Column(name = "uploaded_date", nullable = false, updatable = false)
    private LocalDateTime uploadedDate;

    @PrePersist
    void onCreate() {
        if (uploadedDate == null) {
            uploadedDate = LocalDateTime.now();
        }
    }
}