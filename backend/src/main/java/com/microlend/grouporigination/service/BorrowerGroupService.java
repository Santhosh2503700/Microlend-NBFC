package com.microlend.grouporigination.service;

import com.microlend.grouporigination.dto.GroupRequest;
import com.microlend.grouporigination.dto.GroupResponse;
import com.microlend.grouporigination.dto.GroupSummaryResponse;
import com.microlend.grouporigination.entity.BorrowerGroup;

import java.util.List;


public interface BorrowerGroupService {

    GroupResponse create(Long officerId, GroupRequest req);

    List<GroupResponse> listForOfficer(Long officerId);

    GroupResponse update(Long officerId, Long groupId, GroupRequest req);

    void delete(Long officerId, Long groupId);

    // Aggregate JLG view: per-member loan status + group totals.

    GroupSummaryResponse summary(Long officerId, Long groupId);

    BorrowerGroup getOwnedGroup(Long officerId, Long groupId);

    void refreshMemberCount(Long groupId);
}
