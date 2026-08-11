package com.microlend.analytics.dto;

import java.math.BigDecimal;

// Monthly disbursement volume + cumulative portfolio.
public record PortfolioTrendRow(String month, BigDecimal disbursed, BigDecimal cumulativePortfolio) {
}
