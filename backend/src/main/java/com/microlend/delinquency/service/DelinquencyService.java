package com.microlend.delinquency.service;

import com.microlend.delinquency.dto.BranchStaffResponse;
import com.microlend.delinquency.dto.DelinquencyCaseResponse;
import com.microlend.delinquency.dto.ScanResultResponse;

import java.util.List;
import java.util.Map;


public interface DelinquencyService {

    ScanResultResponse runScan(Long triggeredByUserId);

    void onLoanPaymentApplied(Long loanAccountId);

    List<DelinquencyCaseResponse> listOpenCasesForBranch(Long managerUserId);

    List<BranchStaffResponse> collectionsOfficersInBranch(Long managerUserId);

    DelinquencyCaseResponse assignOfficer(Long managerUserId, Long caseId, Long officerId);

    List<DelinquencyCaseResponse> listCasesForOfficer(Long officerId);

    /**
     * TEST/DEMO (NBFC Admin only): backdate the earliest unpaid installment of a loan by
     * {@code days} so the next delinquency scan flags it — avoids raw SQL when demoing the flow.
     */
    Map<String, Object> backdateEarliestUnpaidInstallment(Long loanAccountId, int days);


    Map<String, Object> generateDemoPortfolioAndScan(Long adminUserId);
}
