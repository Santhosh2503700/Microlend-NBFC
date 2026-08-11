package com.microlend.grouporigination.dto;

import java.math.BigDecimal;
import java.util.List;


public record GroupSummaryResponse(
        Long groupId,
        String groupName,
        Long centreId,
        int memberCount,
        boolean jointLiabilityEnabled,
        BigDecimal totalDisbursed,
        BigDecimal totalOutstanding,
        long overdueMemberCount,
        List<MemberSummary> members
) {
    public record MemberSummary(
            Long borrowerId,
            String name,
            long activeLoans,
            BigDecimal outstandingPrincipal,
            boolean hasOverdue
    ) {
    }
}
