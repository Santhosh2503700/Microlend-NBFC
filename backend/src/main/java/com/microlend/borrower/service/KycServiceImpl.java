package com.microlend.borrower.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.dto.KycResponse;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.enums.DocumentType;
import com.microlend.borrower.enums.KycStatus;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.notification.service.NotificationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final BorrowerKYCRepository kycRepository;
    private final BorrowerRepository borrowerRepository;
    private final KycStorageService storageService;
    private final AuditGateway auditService;
    private final NotificationGateway notificationGateway;

    @Transactional
    public KycResponse upload(Long officerId, Long borrowerId, DocumentType type,
                              MultipartFile file) {
        Borrower borrower = borrowerRepository.findById(borrowerId)
                .orElseThrow(() -> ApiException.notFound("Borrower not found: " + borrowerId));
        if (!borrower.getRegisteredByFieldOfficerId().equals(officerId)) {
            throw ApiException.forbidden("Borrower not registered by this officer");
        }
        String url = storageService.store(file, borrowerId);
        BorrowerKYC kyc = BorrowerKYC.builder()
                .borrowerId(borrowerId)
                .documentType(type)
                .documentFileUrl(url)
                .uploadedByFieldOfficerId(officerId)
                .status(KycStatus.PENDING)
                .build();
        kyc = kycRepository.save(kyc);
        auditService.record(officerId, "KYC_UPLOADED", "KYC",
                "kycId=" + kyc.getKycId() + " borrowerId=" + borrowerId + " type=" + type);
        return KycResponse.from(kyc);
    }

    @Transactional(readOnly = true)
    public List<KycResponse> listForBorrower(Long borrowerId) {
        return kycRepository.findByBorrowerId(borrowerId).stream().map(KycResponse::from).toList();
    }

    @Transactional
    public KycResponse verify(Long verifierId, Long kycId, boolean approve, String remarks) {
        BorrowerKYC kyc = kycRepository.findById(kycId)
                .orElseThrow(() -> ApiException.notFound("KYC not found: " + kycId));
        kyc.setStatus(approve ? KycStatus.VERIFIED : KycStatus.REJECTED);
        kyc.setVerifiedById(verifierId);
        kyc.setVerificationDate(LocalDateTime.now());
        kyc = kycRepository.save(kyc);

        auditService.record(verifierId, approve ? "KYC_VERIFIED" : "KYC_REJECTED", "KYC",
                "kycId=" + kycId + (remarks == null ? "" : " remarks=" + remarks));

        if (!approve) {
            // Notify the uploading Field Officer that KYC was rejected.
            notificationGateway.notifyUser(
                    kyc.getUploadedByFieldOfficerId(),
                    "FIELD_OFFICER",
                    "KYC document (id " + kycId + ") was rejected"
                            + (remarks == null ? "" : ": " + remarks),
                    com.microlend.notification.enums.NotificationCategory.KYC,
                    "BorrowerKYC", kycId);
        }
        return KycResponse.from(kyc);
    }

    @Transactional(readOnly = true)
    public BorrowerKYC getKyc(Long kycId) {
        return kycRepository.findById(kycId)
                .orElseThrow(() -> ApiException.notFound("KYC not found: " + kycId));
    }

    @Transactional(readOnly = true)
    public Path resolveFile(Long kycId) {
        BorrowerKYC kyc = getKyc(kycId);
        if (kyc.getDocumentFileUrl() == null) {
            throw ApiException.notFound("No file stored for this KYC record");
        }
        return storageService.resolve(kyc.getDocumentFileUrl());
    }
}