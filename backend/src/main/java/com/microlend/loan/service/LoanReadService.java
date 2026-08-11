package com.microlend.loan.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.loan.dto.BorrowerDashboardResponse;
import com.microlend.loan.dto.LoanAccountResponse;
import com.microlend.loan.dto.RepaymentScheduleResponse;

import java.util.List;


public interface LoanReadService {

    Borrower requireBorrowerForPortalUser(Long portalUserId);

    BorrowerDashboardResponse dashboardForPortalUser(Long portalUserId);

    List<LoanAccountResponse> loansForPortalUser(Long portalUserId);

    List<RepaymentScheduleResponse> scheduleForPortalUser(Long portalUserId, Long loanAccountId);

    List<LoanAccountResponse> loansForOfficerBorrower(Long officerId, Long borrowerId);

    List<RepaymentScheduleResponse> scheduleForOfficerLoan(Long officerId, Long loanAccountId);
}
