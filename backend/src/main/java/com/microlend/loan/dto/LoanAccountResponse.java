package com.microlend.loan.dto;

import com.microlend.loan.entity.LoanAccount;
import com.microlend.loan.enums.LoanAccountStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanAccountResponse(
        Long loanAccountId,
        Long applicationId,
        Long borrowerId,
        String borrowerName,
        Long productId,
        String productName,
        BigDecimal disbursedAmount,
        LocalDate disbursementDate,
        BigDecimal totalInterest,
        BigDecimal totalRepayable,
        BigDecimal outstandingPrincipal,
        Integer dpd,
        LoanAccountStatus status
) {
    public static LoanAccountResponse from(LoanAccount a, String borrowerName, String productName) {
        return new LoanAccountResponse(a.getLoanAccountId(), a.getApplicationId(), a.getBorrowerId(),
                borrowerName, a.getProductId(), productName, a.getDisbursedAmount(), a.getDisbursementDate(),
                a.getTotalInterest(), a.getTotalRepayable(), a.getOutstandingPrincipal(), a.getDpd(),
                a.getStatus());
    }
}
