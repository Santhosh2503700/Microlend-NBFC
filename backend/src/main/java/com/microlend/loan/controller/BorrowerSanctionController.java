package com.microlend.loan.controller;

import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.SanctionLetterResponse;
import com.microlend.loan.service.SanctionAcceptanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrower/sanction-letters")
@RequiredArgsConstructor
public class BorrowerSanctionController {

    private final SanctionAcceptanceService service;

    @GetMapping
    public List<SanctionLetterResponse> myLetters() {
        return service.listForBorrower(SecurityUtil.currentUserId());
    }

    @PutMapping("/{id}/accept")
    public Object accept(@PathVariable Long id) {
        return service.accept(SecurityUtil.currentUserId(), id);
    }

    @PutMapping("/{id}/reject")
    public SanctionLetterResponse reject(@PathVariable Long id) {
        return service.reject(SecurityUtil.currentUserId(), id);
    }
}
