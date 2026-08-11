package com.microlend.loan.controller;

import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.LoanAccountResponse;
import com.microlend.loan.dto.RepaymentScheduleResponse;
import com.microlend.loan.service.LoanReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/field-officer")
@RequiredArgsConstructor
public class FieldOfficerLoanController {

    private final LoanReadService loanReadService;

    @GetMapping("/borrowers/{borrowerId}/loans")
    public List<LoanAccountResponse> borrowerLoans(@PathVariable Long borrowerId) {
        return loanReadService.loansForOfficerBorrower(SecurityUtil.currentUserId(), borrowerId);
    }

    @GetMapping("/loans/{loanAccountId}/schedule")
    public List<RepaymentScheduleResponse> loanSchedule(@PathVariable Long loanAccountId) {
        return loanReadService.scheduleForOfficerLoan(SecurityUtil.currentUserId(), loanAccountId);
    }
}
