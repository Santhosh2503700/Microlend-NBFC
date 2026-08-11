package com.microlend.delinquency.dto;

import com.microlend.delinquency.enums.CaseStatus;
import com.microlend.delinquency.entity.DelinquencyCase;
import com.microlend.delinquency.enums.ParBucket;

import java.time.LocalDateTime;


public record DelinquencyCaseResponse(
        Long caseId,
        Long loanAccountId,
        Long borrowerId,
        String borrowerName,
        Integer dpd,
        ParBucket parBucket,
        CaseStatus status,
        LocalDateTime openedDate,
        Long assignedCollectionsOfficerId,
        String assignedCollectionsOfficerName,
        LocalDateTime assignedDate,
        Long notifiedBranchManagerId,
        String action
) {
    public static DelinquencyCaseResponse from(DelinquencyCase c, Long borrowerId, String borrowerName,
                                               String assignedOfficerName) {
        return new DelinquencyCaseResponse(
                c.getCaseId(), c.getLoanAccountId(), borrowerId, borrowerName,
                c.getDpd(), c.getParBucket(), c.getStatus(), c.getOpenedDate(),
                c.getAssignedCollectionsOfficerId(), assignedOfficerName, c.getAssignedDate(),
                c.getNotifiedBranchManagerId(), c.getAction());
    }
}
