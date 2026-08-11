package com.microlend.loan.service;

import com.microlend.loan.enums.InterestType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiCalculationServiceImpl implements EmiCalculationService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_UP);
    private static final int MONEY_SCALE = 2;

    @Override
    public BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent,
                                int tenureMonths, InterestType type) {
        validate(principal, annualRatePercent, tenureMonths);

        if (type == InterestType.FLAT) {
            BigDecimal years = BigDecimal.valueOf(tenureMonths).divide(BigDecimal.valueOf(12), MC);
            BigDecimal totalInterest = principal.multiply(annualRatePercent, MC)
                    .divide(BigDecimal.valueOf(100), MC)
                    .multiply(years, MC);
            BigDecimal totalRepayable = principal.add(totalInterest, MC);
            return totalRepayable.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }

        // Reducing balance
        BigDecimal r = annualRatePercent
                .divide(BigDecimal.valueOf(12), MC)
                .divide(BigDecimal.valueOf(100), MC);
        if (r.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal onePlusR = BigDecimal.ONE.add(r, MC);
        BigDecimal pow = onePlusR.pow(tenureMonths, MC);
        BigDecimal emi = principal.multiply(r, MC).multiply(pow, MC)
                .divide(pow.subtract(BigDecimal.ONE, MC), MC);
        return emi.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public AmortizationResult amortizationSchedule(BigDecimal principal, BigDecimal annualRatePercent,
                                                   int tenureMonths, InterestType type) {
        validate(principal, annualRatePercent, tenureMonths);
        BigDecimal emi = calculate(principal, annualRatePercent, tenureMonths, type);
        List<AmortizationRow> rows = new ArrayList<>(tenureMonths);

        if (type == InterestType.FLAT) {
            BigDecimal years = BigDecimal.valueOf(tenureMonths).divide(BigDecimal.valueOf(12), MC);
            BigDecimal totalInterest = principal.multiply(annualRatePercent, MC)
                    .divide(BigDecimal.valueOf(100), MC).multiply(years, MC)
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal perInterest = totalInterest.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal perPrincipal = principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);

            BigDecimal principalAcc = BigDecimal.ZERO;
            BigDecimal interestAcc = BigDecimal.ZERO;
            for (int i = 1; i <= tenureMonths; i++) {
                BigDecimal p = perPrincipal;
                BigDecimal in = perInterest;
                if (i == tenureMonths) {
                    p = principal.subtract(principalAcc);            // absorb residue
                    in = totalInterest.subtract(interestAcc);
                }
                principalAcc = principalAcc.add(p);
                interestAcc = interestAcc.add(in);
                rows.add(new AmortizationRow(i, p, in, p.add(in)));
            }
            return new AmortizationResult(emi, principal, totalInterest,
                    principal.add(totalInterest), rows);
        }

        // Reducing balance amortization
        BigDecimal r = annualRatePercent.divide(BigDecimal.valueOf(12), MC).divide(BigDecimal.valueOf(100), MC);
        BigDecimal outstanding = principal;
        BigDecimal totalInterest = BigDecimal.ZERO;
        for (int i = 1; i <= tenureMonths; i++) {
            BigDecimal interest = outstanding.multiply(r, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            BigDecimal principalComponent;
            BigDecimal total;
            if (i == tenureMonths) {
                principalComponent = outstanding;                    // clear the balance exactly
                total = principalComponent.add(interest);
            } else {
                principalComponent = emi.subtract(interest);
                total = emi;
            }
            outstanding = outstanding.subtract(principalComponent);
            totalInterest = totalInterest.add(interest);
            rows.add(new AmortizationRow(i, principalComponent, interest, total));
        }
        return new AmortizationResult(emi, principal, totalInterest,
                principal.add(totalInterest), rows);
    }

    private void validate(BigDecimal principal, BigDecimal ratePercent, int tenureMonths) {
        if (principal == null || principal.signum() <= 0) {
            throw new IllegalArgumentException("Loan principal must be > 0");
        }
        if (ratePercent == null || ratePercent.signum() < 0) {
            throw new IllegalArgumentException("Interest rate must be >= 0");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure (months) must be > 0");
        }
    }
}
