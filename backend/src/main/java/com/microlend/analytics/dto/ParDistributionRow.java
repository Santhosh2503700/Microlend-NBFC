package com.microlend.analytics.dto;

import java.math.BigDecimal;

// One PAR bucket's live count + outstanding exposure.
public record ParDistributionRow(String bucket, long count, BigDecimal outstanding) {
}
