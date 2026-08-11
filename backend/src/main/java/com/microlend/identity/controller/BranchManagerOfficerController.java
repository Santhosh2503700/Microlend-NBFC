package com.microlend.identity.controller;

import com.microlend.identity.dto.OfficerRosterResponse;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.identity.service.UserDirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branch-manager/officers")
@RequiredArgsConstructor
public class BranchManagerOfficerController {

    private final UserDirectoryService userDirectoryService;

    @GetMapping
    public List<OfficerRosterResponse> roster() {
        return userDirectoryService.rosterForManager(SecurityUtil.currentUserId());
    }
}
