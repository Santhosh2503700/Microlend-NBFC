package com.microlend.loan.controller;

import com.microlend.loan.dto.LoanProductResponse;
import com.microlend.loan.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductPublicController {

    private final LoanProductService service;

    @GetMapping
    public List<LoanProductResponse> listActive() {
        return service.findActive();
    }
}
