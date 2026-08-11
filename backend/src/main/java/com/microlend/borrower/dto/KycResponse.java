package com.microlend.borrower.dto;

import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.enums.DocumentType;
import com.microlend.borrower.enums.KycStatus;

import java.time.LocalDateTime;

public record KycResponse(
        Long kycId,
        Long borrowerId,
        DocumentType documentType,
        String documentFileUrl,
        Long uploadedByFieldOfficerId,
        Long verifiedById,
        LocalDateTime verificationDate,
        KycStatus status,
        LocalDateTime uploadedDate
) {
    public static KycResponse from(BorrowerKYC k) {
        return new KycResponse(k.getKycId(), k.getBorrowerId(), k.getDocumentType(),
                k.getDocumentFileUrl(), k.getUploadedByFieldOfficerId(), k.getVerifiedById(),
                k.getVerificationDate(), k.getStatus(), k.getUploadedDate());
    }
}