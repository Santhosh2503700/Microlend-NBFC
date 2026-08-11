package com.microlend.analytics.dto;

import java.math.BigDecimal;

// Monthly Net NPA %. Time-bucketed by loan disbursement month using current outstanding/DPD

public record NpaTrendRow(String month, BigDecimal npaPercent, BigDecimal npaAmount,
                          BigDecimal totalOutstanding) {
}
