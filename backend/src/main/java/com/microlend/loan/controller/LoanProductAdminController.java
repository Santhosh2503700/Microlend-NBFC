package com.microlend.loan.controller;

import com.microlend.audit.service.AuditGateway;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.loan.dto.EmiPreviewResponse;
import com.microlend.loan.dto.LoanProductRequest;
import com.microlend.loan.dto.LoanProductResponse;
import com.microlend.loan.service.LoanProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loan-products")
@RequiredArgsConstructor
public class LoanProductAdminController {

    private final LoanProductService service;
    private final AuditGateway auditService;

    @PostMapping
    public LoanProductResponse create(@Valid @RequestBody LoanProductRequest req) {
        LoanProductResponse res = service.create(req);
        auditService.record(SecurityUtil.currentUserId(), "LOAN_PRODUCT_CREATED", "LOAN_PRODUCT",
                "productId=" + res.productId() + " name=" + res.productName());
        return res;
    }

    @GetMapping
    public List<LoanProductResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public LoanProductResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public LoanProductResponse update(@PathVariable Long id, @Valid @RequestBody LoanProductRequest req) {
        LoanProductResponse res = service.update(id, req);
        auditService.record(SecurityUtil.currentUserId(), "LOAN_PRODUCT_UPDATED", "LOAN_PRODUCT",
                "productId=" + id);
        return res;
    }

    @GetMapping("/{id}/emi-preview")
    public EmiPreviewResponse emiPreview(@PathVariable Long id) {
        return service.emiPreview(id);
    }
}
