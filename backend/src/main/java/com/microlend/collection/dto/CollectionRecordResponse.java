package com.microlend.collection.dto;

import com.microlend.collection.enums.CollectionMode;
import com.microlend.collection.entity.CollectionRecord;
import com.microlend.collection.enums.CollectionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

// Always carries the Borrower's Name
public record CollectionRecordResponse(
        Long collectionId,
        Long loanAccountId,
        Long scheduleId,
        Long borrowerId,
        String borrowerName,
        BigDecimal collectedAmount,
        LocalDate collectionDate,
        Long collectedById,
        CollectionMode mode,
        CollectionStatus status,
        Long receiptId
) {
    public static CollectionRecordResponse from(CollectionRecord r, Long borrowerId,
                                                String borrowerName, Long receiptId) {
        return new CollectionRecordResponse(r.getCollectionId(), r.getLoanAccountId(), r.getScheduleId(),
                borrowerId, borrowerName, r.getCollectedAmount(), r.getCollectionDate(),
                r.getCollectedById(), r.getMode(), r.getStatus(), receiptId);
    }
}
