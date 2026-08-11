package com.microlend.delinquency.controller;

import com.microlend.delinquency.dto.DelinquencyCaseResponse;
import com.microlend.delinquency.service.DelinquencyService;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/collections-officer")
@RequiredArgsConstructor
public class CollectionsOfficerCaseController {

    private final DelinquencyService service;

    @GetMapping("/cases")
    public List<DelinquencyCaseResponse> myCases() {
        return service.listCasesForOfficer(SecurityUtil.currentUserId());
    }
}
