package com.microlend.loan.service;

import com.microlend.loan.enums.InterestType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pure unit tests for the single source of truth for EMI math. */
class EmiCalculationServiceImplTest {

    private final EmiCalculationService svc = new EmiCalculationServiceImpl();

    @Test
    void reducingBalanceEmiPositiveAndPrincipalReconciles() {
        BigDecimal principal = new BigDecimal("50000");
        BigDecimal emi = svc.calculate(principal, new BigDecimal("18"), 12, InterestType.REDUCING_BALANCE);
        assertThat(emi).isGreaterThan(BigDecimal.ZERO);

        AmortizationResult am = svc.amortizationSchedule(principal, new BigDecimal("18"), 12, InterestType.REDUCING_BALANCE);
        assertThat(am.getRows()).hasSize(12);
        BigDecimal principalSum = am.getRows().stream()
                .map(AmortizationRow::getPrincipalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(principalSum).isEqualByComparingTo(principal);
        assertThat(am.getTotalRepayable()).isEqualByComparingTo(principal.add(am.getTotalInterest()));
    }

    @Test
    void flatInterestScheduleReconciles() {
        BigDecimal principal = new BigDecimal("60000");
        AmortizationResult am = svc.amortizationSchedule(principal, new BigDecimal("22"), 18, InterestType.FLAT);
        assertThat(am.getRows()).hasSize(18);
        BigDecimal principalSum = am.getRows().stream()
                .map(AmortizationRow::getPrincipalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(principalSum).isEqualByComparingTo(principal);
    }

    @Test
    void rejectsNonPositivePrincipal() {
        assertThatThrownBy(() -> svc.calculate(BigDecimal.ZERO, new BigDecimal("10"), 12, InterestType.FLAT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonPositiveTenure() {
        assertThatThrownBy(() -> svc.calculate(new BigDecimal("1000"), new BigDecimal("10"), 0, InterestType.FLAT))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
