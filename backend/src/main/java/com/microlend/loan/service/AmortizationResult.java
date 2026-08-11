package com.microlend.loan.service;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class AmortizationResult {
    BigDecimal emi;
    BigDecimal totalPrincipal;
    BigDecimal totalInterest;
    BigDecimal totalRepayable;
    List<AmortizationRow> rows;
}
