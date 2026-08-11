package com.microlend.analytics.service;

import com.microlend.analytics.dto.*;
import com.microlend.identity.enums.Role;

import java.util.List;


public interface AnalyticsService {

    List<ParDistributionRow> parDistribution(Long userId, Role role, String scope);

    List<PortfolioTrendRow> portfolioTrend(Long userId, Role role, String scope);

    List<CollectionEfficiencyRow> collectionEfficiency(Long userId, Role role, String scope);

    List<OfficerPerformanceRow> officerPerformance(Long userId, Role role, String scope);

    List<LoanFunnelRow> loanFunnel(Long userId, Role role, String scope);

    List<NpaTrendRow> npaTrend(Long userId, Role role, String scope);
}
