package com.microlend.borrower.controller;

import com.microlend.borrower.dto.BorrowerResponse;
import com.microlend.borrower.dto.KycResponse;
import com.microlend.borrower.service.BorrowerService;
import com.microlend.borrower.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/credit-officer/borrowers")
@RequiredArgsConstructor
public class CreditOfficerBorrowerController {

    private final BorrowerService borrowerService;
    private final KycService kycService;


    @GetMapping("/pending-kyc")
    public List<Map<String, Object>> pendingKycBorrowers() {
        return borrowerService.pendingKycBorrowers();
    }

    @GetMapping("/{borrowerId}")
    public BorrowerResponse borrower(@PathVariable Long borrowerId) {
        return borrowerService.getByIdPrivileged(borrowerId);
    }

    @GetMapping("/{borrowerId}/kyc")
    public List<KycResponse> kyc(@PathVariable Long borrowerId) {
        return kycService.listForBorrower(borrowerId);
    }
}