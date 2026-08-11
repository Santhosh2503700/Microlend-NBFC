package com.microlend.borrower.controller;

import com.microlend.borrower.dto.BorrowerRegistrationRequest;
import com.microlend.borrower.dto.BorrowerRegistrationResponse;
import com.microlend.borrower.dto.BorrowerResponse;
import com.microlend.borrower.dto.KycResponse;
import com.microlend.borrower.enums.DocumentType;
import com.microlend.borrower.service.BorrowerService;
import com.microlend.borrower.service.KycService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/field-officer/borrowers")
@RequiredArgsConstructor
public class FieldOfficerBorrowerController {

    private final BorrowerService borrowerService;
    private final KycService kycService;

    @PostMapping
    public BorrowerRegistrationResponse register(@Valid @RequestBody BorrowerRegistrationRequest req) {
        return borrowerService.register(SecurityUtil.currentUserId(), req);
    }

    @GetMapping
    public List<BorrowerResponse> myBorrowers() {
        return borrowerService.listForOfficer(SecurityUtil.currentUserId());
    }

    @GetMapping("/{borrowerId}")
    public BorrowerResponse get(@PathVariable Long borrowerId) {
        return borrowerService.getForOfficer(SecurityUtil.currentUserId(), borrowerId);
    }

    @PostMapping(value = "/{borrowerId}/kyc", consumes = "multipart/form-data")
    public KycResponse uploadKyc(@PathVariable Long borrowerId,
                                 @RequestParam("documentType") DocumentType documentType,
                                 @RequestPart("file") MultipartFile file) {
        return kycService.upload(SecurityUtil.currentUserId(), borrowerId, documentType, file);
    }

    @GetMapping("/{borrowerId}/kyc")
    public List<KycResponse> listKyc(@PathVariable Long borrowerId) {
        // Ownership enforced via borrower lookup.
        borrowerService.getForOfficer(SecurityUtil.currentUserId(), borrowerId);
        return kycService.listForBorrower(borrowerId);
    }
}