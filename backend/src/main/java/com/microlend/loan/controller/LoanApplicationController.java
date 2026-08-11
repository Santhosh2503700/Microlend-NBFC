package com.microlend.loan.controller;

import com.microlend.identity.security.AppUserPrincipal;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.LoanApplicationRequest;
import com.microlend.loan.dto.LoanApplicationResponse;
import com.microlend.loan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService service;

    @PostMapping
    public LoanApplicationResponse submit(@Valid @RequestBody LoanApplicationRequest req) {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.submit(p.userId(), p.role(), req);
    }

    @GetMapping
    public List<LoanApplicationResponse> list() {
        AppUserPrincipal p = SecurityUtil.currentPrincipal();
        return service.listForRole(p.userId(), p.role(), null);
    }
}
