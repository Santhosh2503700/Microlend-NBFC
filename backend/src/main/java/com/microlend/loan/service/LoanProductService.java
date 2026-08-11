package com.microlend.loan.service;

import com.microlend.loan.dto.EmiPreviewResponse;
import com.microlend.loan.dto.LoanProductRequest;
import com.microlend.loan.dto.LoanProductResponse;
import com.microlend.loan.entity.LoanProduct;

import java.util.List;


public interface LoanProductService {

    LoanProductResponse create(LoanProductRequest req);

    LoanProductResponse update(Long id, LoanProductRequest req);

    List<LoanProductResponse> findAll();

    List<LoanProductResponse> findActive();

    LoanProductResponse findById(Long id);

    LoanProduct getEntity(Long id);

    EmiPreviewResponse emiPreview(Long id);
}
