package com.microlend.collection.controller;

import com.microlend.collection.dto.DisputeRequest;
import com.microlend.collection.dto.ReceiptResponse;
import com.microlend.collection.service.CollectionService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrower/receipts")
@RequiredArgsConstructor
public class BorrowerReceiptController {

    private final CollectionService service;

    @GetMapping
    public List<ReceiptResponse> inbox(@RequestParam(name = "all", defaultValue = "false") boolean all) {
        return service.borrowerReceipts(SecurityUtil.currentUserId(), !all);
    }

    @PutMapping("/{id}/approve")
    public ReceiptResponse approve(@PathVariable Long id) {

        return service.approve(SecurityUtil.currentUserId(), id);
    }

    @PutMapping("/{id}/dispute")
    public ReceiptResponse dispute(@PathVariable Long id, @Valid @RequestBody DisputeRequest req) {
        return service.dispute(SecurityUtil.currentUserId(), id, req.disputeRemarks());
    }
}
