package com.microlend.borrower.service;

import com.microlend.borrower.entity.CreditAssessment;
import com.microlend.loan.entity.LoanProduct;

import java.math.BigDecimal;


public interface CreditAssessmentService {

    CreditAssessment assess(Long applicationId, Long borrowerId, LoanProduct product, BigDecimal requestedAmount);
}
