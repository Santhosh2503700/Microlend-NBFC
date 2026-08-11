package com.microlend.collection.dto;

import com.microlend.collection.enums.BorrowerApprovalStatus;
import com.microlend.collection.enums.CollectionMode;
import com.microlend.collection.entity.CollectionReceipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptResponse(
        Long receiptId,
        Long collectionId,
        Long loanAccountId,
        Long borrowerId,
        String borrowerName,
        Long fieldOfficerId,
        BigDecimal statedAmount,
        LocalDate collectionDate,
        CollectionMode mode,
        BorrowerApprovalStatus borrowerApprovalStatus,
        LocalDateTime borrowerApprovedDate,
        String disputeRemarks,
        Long coSignedById,
        String coSignJustification,
        boolean coSigned,
        LocalDateTime generatedTimestamp
) {
    public static ReceiptResponse from(CollectionReceipt r, String borrowerName) {
        boolean coSigned = r.getBorrowerApprovalStatus() == BorrowerApprovalStatus.CO_SIGNED;
        return new ReceiptResponse(r.getReceiptId(), r.getCollectionId(), r.getLoanAccountId(),
                r.getBorrowerId(), borrowerName, r.getFieldOfficerId(), r.getStatedAmount(),
                r.getCollectionDate(), r.getMode(), r.getBorrowerApprovalStatus(),
                r.getBorrowerApprovedDate(), r.getDisputeRemarks(), r.getCoSignedById(),
                r.getCoSignJustification(), coSigned, r.getGeneratedTimestamp());
    }
}
