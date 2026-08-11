package com.microlend.analytics.controller;

import com.microlend.analytics.dto.*;
import com.microlend.analytics.service.AnalyticsService;
import com.microlend.identity.security.AppUserPrincipal;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/par-distribution")
    public List<ParDistributionRow> parDistribution(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.parDistribution(p.userId(), p.role(), scope);
    }

    @GetMapping("/portfolio-trend")
    public List<PortfolioTrendRow> portfolioTrend(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.portfolioTrend(p.userId(), p.role(), scope);
    }

    @GetMapping("/collection-efficiency")
    public List<CollectionEfficiencyRow> collectionEfficiency(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.collectionEfficiency(p.userId(), p.role(), scope);
    }

    @GetMapping("/officer-performance")
    public List<OfficerPerformanceRow> officerPerformance(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.officerPerformance(p.userId(), p.role(), scope);
    }

    @GetMapping("/loan-funnel")
    public List<LoanFunnelRow> loanFunnel(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.loanFunnel(p.userId(), p.role(), scope);
    }

    @GetMapping("/npa-trend")
    public List<NpaTrendRow> npaTrend(@RequestParam(required = false) String scope) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.npaTrend(p.userId(), p.role(), scope);
    }
}
