package com.microlend.grouporigination.dto;

import com.microlend.grouporigination.entity.BorrowerGroup;
import com.microlend.grouporigination.enums.CommonStatus;

import java.time.LocalDate;

public record GroupResponse(
        Long groupId,
        String groupName,
        Long centreId,
        Long createdByFieldOfficerId,
        LocalDate formationDate,
        Integer memberCount,
        boolean jointLiabilityEnabled,
        CommonStatus status
) {
    public static GroupResponse from(BorrowerGroup g) {
        return new GroupResponse(g.getGroupId(), g.getGroupName(), g.getCentreId(),
                g.getCreatedByFieldOfficerId(), g.getFormationDate(), g.getMemberCount(),
                g.isJointLiabilityEnabled(), g.getStatus());
    }
}
