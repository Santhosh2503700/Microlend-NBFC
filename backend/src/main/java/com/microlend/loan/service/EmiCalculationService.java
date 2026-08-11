package com.microlend.loan.service;

import com.microlend.loan.enums.InterestType;

import java.math.BigDecimal;


public interface EmiCalculationService {

    BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths, InterestType type);

    AmortizationResult amortizationSchedule(BigDecimal principal, BigDecimal annualRatePercent,
                                            int tenureMonths, InterestType type);
}
