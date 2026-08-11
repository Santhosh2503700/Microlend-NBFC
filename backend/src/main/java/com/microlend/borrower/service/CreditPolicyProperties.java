package com.microlend.borrower.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
@ConfigurationProperties(prefix = "microlend.credit-policy")
@Getter
@Setter
public class CreditPolicyProperties {

    // Score weights (out of 100)
    private int affordabilityWeight = 40;
    private int exposureWeight = 20;
    private int historyWeight = 25;
    private int kycWeight = 15;

    // Bucket thresholds
    private BigDecimal greenMinScore = BigDecimal.valueOf(70);
    private BigDecimal amberMinScore = BigDecimal.valueOf(50);
    private BigDecimal maxDbrGreen = BigDecimal.valueOf(0.40);
    private BigDecimal maxDbrAmber = BigDecimal.valueOf(0.55);

    // Affordability: prospective-EMI/income ratio bounds (<=low => full marks, >=high => zero)
    private BigDecimal affordabilityLowRatio = BigDecimal.valueOf(0.20);
    private BigDecimal affordabilityHighRatio = BigDecimal.valueOf(0.60);
}
