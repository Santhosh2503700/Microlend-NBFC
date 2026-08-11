package com.microlend.borrower.controller;

import com.microlend.borrower.dto.BorrowerResponse;
import com.microlend.borrower.service.BorrowerService;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/branch-manager/borrowers")
@RequiredArgsConstructor
public class BranchManagerBorrowerController {

    private final BorrowerService borrowerService;

    @GetMapping
    public List<BorrowerResponse> branchBorrowers() {
        return borrowerService.listForBranch(SecurityUtil.currentUserId());
    }
}
