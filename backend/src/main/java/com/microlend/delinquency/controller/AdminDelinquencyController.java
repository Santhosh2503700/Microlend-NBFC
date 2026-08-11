package com.microlend.delinquency.controller;

import com.microlend.delinquency.dto.ScanResultResponse;
import com.microlend.delinquency.service.DelinquencyService;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/admin/delinquency")
@RequiredArgsConstructor
public class AdminDelinquencyController {

    private final DelinquencyService service;

    @PostMapping("/run")
    public ScanResultResponse runNow() {
        return service.runScan(SecurityUtil.currentUserId());
    }


    @PostMapping("/backdate")
    public Map<String, Object> backdate(@RequestParam Long loanAccountId,
                                        @RequestParam(defaultValue = "45") int days) {
        return service.backdateEarliestUnpaidInstallment(loanAccountId, days);
    }

    //TEST/DEMO: seed a small demo portfolio spread across PAR30/PAR60/PAR90/PAR180.
    @PostMapping("/demo-portfolio")
    public Map<String, Object> demo() {
        return service.generateDemoPortfolioAndScan(SecurityUtil.currentUserId());
    }
}
