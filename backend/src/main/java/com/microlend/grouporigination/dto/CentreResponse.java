package com.microlend.grouporigination.dto;

import com.microlend.grouporigination.entity.Centre;
import com.microlend.grouporigination.enums.CommonStatus;

import java.time.LocalTime;

public record CentreResponse(
        Long centreId,
        String centreName,
        Long branchId,
        Long createdByFieldOfficerId,
        String village,
        String meetingDay,
        LocalTime meetingTime,
        CommonStatus status
) {
    public static CentreResponse from(Centre c) {
        return new CentreResponse(c.getCentreId(), c.getCentreName(), c.getBranchId(),
                c.getCreatedByFieldOfficerId(), c.getVillage(), c.getMeetingDay(),
                c.getMeetingTime(), c.getStatus());
    }
}
