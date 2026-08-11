package com.microlend.borrower.service;

import com.microlend.borrower.dto.BorrowerRegistrationRequest;
import com.microlend.borrower.dto.BorrowerRegistrationResponse;
import com.microlend.borrower.dto.BorrowerResponse;
import com.microlend.borrower.entity.Borrower;

import java.util.List;
import java.util.Map;


public interface BorrowerService {

    BorrowerRegistrationResponse register(Long officerId, BorrowerRegistrationRequest req);

    List<BorrowerResponse> listForOfficer(Long officerId);

    BorrowerResponse getForOfficer(Long officerId, Long borrowerId);

    List<BorrowerResponse> listForBranch(Long managerUserId);

    /** Fetch a borrower enforcing the Field Officer owns it (rule 4). */
    Borrower getOwned(Long officerId, Long borrowerId);

    BorrowerResponse getByIdPrivileged(Long borrowerId);

    String officerName(Long officerId);

    List<Map<String, Object>> pendingKycBorrowers();
}
