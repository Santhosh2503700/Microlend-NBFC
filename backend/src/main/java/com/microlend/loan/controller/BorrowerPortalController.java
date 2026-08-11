package com.microlend.loan.controller;

import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.BorrowerDashboardResponse;
import com.microlend.loan.dto.LoanAccountResponse;
import com.microlend.loan.dto.RepaymentScheduleResponse;
import com.microlend.loan.service.LoanReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/borrower")
@RequiredArgsConstructor
public class BorrowerPortalController {

    private final LoanReadService loanReadService;

    @GetMapping("/dashboard")
    public BorrowerDashboardResponse dashboard() {
        return loanReadService.dashboardForPortalUser(SecurityUtil.currentUserId());
    }

    @GetMapping("/loans")
    public List<LoanAccountResponse> loans() {
        return loanReadService.loansForPortalUser(SecurityUtil.currentUserId());
    }

    @GetMapping("/loans/{loanAccountId}/schedule")
    public List<RepaymentScheduleResponse> schedule(@PathVariable Long loanAccountId) {
        return loanReadService.scheduleForPortalUser(SecurityUtil.currentUserId(), loanAccountId);
    }
}
