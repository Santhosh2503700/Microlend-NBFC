package com.microlend.grouporigination.controller;

import com.microlend.audit.service.AuditGateway;
import com.microlend.grouporigination.dto.CentreRequest;
import com.microlend.grouporigination.dto.CentreResponse;
import com.microlend.grouporigination.dto.GroupRequest;
import com.microlend.grouporigination.dto.GroupResponse;
import com.microlend.grouporigination.dto.GroupSummaryResponse;
import com.microlend.grouporigination.service.BorrowerGroupService;
import com.microlend.grouporigination.service.CentreService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field-officer")
@RequiredArgsConstructor
public class FieldOfficerOriginationController {

    private final CentreService centreService;
    private final BorrowerGroupService groupService;
    private final AuditGateway auditService;

    @PostMapping("/centres")
    public CentreResponse createCentre(@Valid @RequestBody CentreRequest req) {
        Long officerId = SecurityUtil.currentUserId();
        CentreResponse res = centreService.create(officerId, req);
        auditService.record(officerId, "CENTRE_CREATED", "CENTRE", "centreId=" + res.centreId());
        return res;
    }

    @GetMapping("/centres")
    public List<CentreResponse> listCentres() {
        return centreService.listForOfficer(SecurityUtil.currentUserId());
    }

    @PutMapping("/centres/{id}")
    public CentreResponse updateCentre(@PathVariable Long id, @Valid @RequestBody CentreRequest req) {
        Long officerId = SecurityUtil.currentUserId();
        CentreResponse res = centreService.update(officerId, id, req);
        auditService.record(officerId, "CENTRE_UPDATED", "CENTRE", "centreId=" + id);
        return res;
    }

    @DeleteMapping("/centres/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //204
    public void deleteCentre(@PathVariable Long id) {
        Long officerId = SecurityUtil.currentUserId();
        centreService.delete(officerId, id);
        auditService.record(officerId, "CENTRE_DELETED", "CENTRE", "centreId=" + id);
    }

    @PostMapping("/groups")
    public GroupResponse createGroup(@Valid @RequestBody GroupRequest req) {
        Long officerId = SecurityUtil.currentUserId();
        GroupResponse res = groupService.create(officerId, req);
        auditService.record(officerId, "GROUP_CREATED", "GROUP", "groupId=" + res.groupId());
        return res;
    }

    @GetMapping("/groups")
    public List<GroupResponse> listGroups() {
        return groupService.listForOfficer(SecurityUtil.currentUserId());
    }

    @PutMapping("/groups/{id}")
    public GroupResponse updateGroup(@PathVariable Long id, @Valid @RequestBody GroupRequest req) {
        Long officerId = SecurityUtil.currentUserId();
        GroupResponse res = groupService.update(officerId, id, req);
        auditService.record(officerId, "GROUP_UPDATED", "GROUP", "groupId=" + id);
        return res;
    }

    @DeleteMapping("/groups/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable Long id) {
        Long officerId = SecurityUtil.currentUserId();
        groupService.delete(officerId, id);
        auditService.record(officerId, "GROUP_DELETED", "GROUP", "groupId=" + id);
    }

    // JLG aggregate view — per-member loan status + group totals.
    @GetMapping("/groups/{id}/summary")
    public GroupSummaryResponse groupSummary(@PathVariable Long id) {
        return groupService.summary(SecurityUtil.currentUserId(), id);
    }
}
