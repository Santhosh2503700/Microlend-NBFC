package com.microlend.loan.controller;

import com.microlend.identity.enums.Role;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.AssessmentResponse;
import com.microlend.loan.dto.DecisionRequest;
import com.microlend.loan.dto.LoanApplicationResponse;
import com.microlend.loan.enums.ApplicationStatus;
import com.microlend.loan.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credit-officer/applications")
@RequiredArgsConstructor
public class CreditOfficerApplicationController {

    private final LoanApplicationService service;

    @GetMapping
    public List<LoanApplicationResponse> queue() {
        return service.listForRole(SecurityUtil.currentUserId(), Role.CREDIT_OFFICER, null);
    }

    @GetMapping("/waitlisted")
    public List<LoanApplicationResponse> waitlisted() {
        return service.listForRole(SecurityUtil.currentUserId(), Role.CREDIT_OFFICER, null).stream()
                .filter(a -> a.status() == ApplicationStatus.WAITLISTED)
                .toList();
    }

    @GetMapping("/{id}/assessment")
    public AssessmentResponse assessment(@PathVariable Long id) {
        return service.getAssessment(id);
    }

    @PutMapping("/{id}/decision")
    public Map<String, Object> decide(@PathVariable Long id, @Valid @RequestBody DecisionRequest req) {
        return service.decide(SecurityUtil.currentUserId(), id, req);
    }
}
