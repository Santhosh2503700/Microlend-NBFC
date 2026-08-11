package com.microlend.analytics.dto;

import java.math.BigDecimal;

// Monthly due vs. collected, with efficiency % (collected / due).
public record CollectionEfficiencyRow(String month, BigDecimal due, BigDecimal collected,
                                      BigDecimal efficiencyPercent) {
}
