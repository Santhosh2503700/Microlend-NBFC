package com.microlend.loan.dto;

import com.microlend.loan.enums.ApplicationStatus;
import com.microlend.loan.entity.LoanApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanApplicationResponse(
        Long applicationId,
        Long borrowerId,
        String borrowerName,
        Long groupId,
        Long loanProductId,
        String loanProductName,
        BigDecimal requestedAmount,
        String purpose,
        LocalDateTime applicationDate,
        Long creditOfficerId,
        ApplicationStatus status
) {
    public static LoanApplicationResponse from(LoanApplication a, String borrowerName, String productName) {
        return new LoanApplicationResponse(a.getApplicationId(), a.getBorrowerId(), borrowerName,
                a.getGroupId(), a.getLoanProductId(), productName, a.getRequestedAmount(),
                a.getPurpose(), a.getApplicationDate(), a.getCreditOfficerId(), a.getStatus());
    }
}
