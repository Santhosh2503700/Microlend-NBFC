package com.microlend.collection.controller;

import com.microlend.collection.dto.CollectionRecordResponse;
import com.microlend.collection.dto.CollectionRequest;
import com.microlend.collection.service.CollectionService;
import com.microlend.identity.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/field-officer/collections")
@RequiredArgsConstructor
public class FieldOfficerCollectionController {

    private final CollectionService service;

    @PostMapping
    public CollectionRecordResponse record(@Valid @RequestBody CollectionRequest req) {
        return service.record(SecurityUtil.currentUserId(), req);
    }

    @GetMapping
    public List<CollectionRecordResponse> myCollections() {
        return service.listForOfficer(SecurityUtil.currentUserId());
    }
}
