package com.microlend.borrower.service;

import com.microlend.borrower.dto.KycResponse;
import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;


public interface KycService {

    KycResponse upload(Long officerId, Long borrowerId, DocumentType type, MultipartFile file);

    List<KycResponse> listForBorrower(Long borrowerId);

    KycResponse verify(Long verifierId, Long kycId, boolean approve, String remarks);

    BorrowerKYC getKyc(Long kycId);

    Path resolveFile(Long kycId);
}