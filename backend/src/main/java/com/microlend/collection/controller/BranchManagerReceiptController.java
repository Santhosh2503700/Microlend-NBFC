package com.microlend.collection.controller;

import com.microlend.collection.dto.CoSignRequest;
import com.microlend.collection.dto.ReceiptResponse;
import com.microlend.collection.service.CollectionService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/branch-manager")
@RequiredArgsConstructor
public class BranchManagerReceiptController {

    private final CollectionService service;

    @GetMapping("/receipt-disputes")
    public List<ReceiptResponse> disputes() {
        return service.branchDisputes(SecurityUtil.currentUserId());
    }

    @PutMapping("/receipts/{id}/co-sign")
    public ReceiptResponse coSign(@PathVariable Long id, @Valid @RequestBody CoSignRequest req) {
        return service.coSign(SecurityUtil.currentUserId(), id, req.justification());
    }
}
