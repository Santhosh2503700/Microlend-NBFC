package com.microlend.loan.service;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class AmortizationRow {
    int installmentNumber;
    BigDecimal principalDue;
    BigDecimal interestDue;
    BigDecimal totalDue;
}
