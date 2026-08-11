package com.microlend.delinquency.controller;

import com.microlend.delinquency.dto.AssignOfficerRequest;
import com.microlend.delinquency.dto.BranchStaffResponse;
import com.microlend.delinquency.dto.DelinquencyCaseResponse;
import com.microlend.delinquency.service.DelinquencyService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/branch-manager")
@RequiredArgsConstructor
public class BranchManagerDelinquencyController {

    private final DelinquencyService service;

    @GetMapping("/delinquency-cases")
    public List<DelinquencyCaseResponse> openCases() {
        return service.listOpenCasesForBranch(SecurityUtil.currentUserId());
    }

    @GetMapping("/collections-officers")
    public List<BranchStaffResponse> branchCollectionsOfficers() {
        return service.collectionsOfficersInBranch(SecurityUtil.currentUserId());
    }

    @PutMapping("/delinquency-cases/{id}/assign")
    public DelinquencyCaseResponse assign(@PathVariable Long id,
                                          @Valid @RequestBody AssignOfficerRequest req) {
        return service.assignOfficer(SecurityUtil.currentUserId(), id, req.collectionsOfficerId());
    }
}
