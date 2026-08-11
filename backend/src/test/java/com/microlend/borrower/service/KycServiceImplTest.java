package com.microlend.borrower.service;

import com.microlend.audit.service.AuditGateway;
import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.enums.DocumentType;
import com.microlend.borrower.repository.BorrowerKYCRepository;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.notification.service.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KycServiceImplTest {

    @Mock BorrowerKYCRepository kycRepository;
    @Mock BorrowerRepository borrowerRepository;
    @Mock KycStorageService storageService;
    @Mock AuditGateway auditService;
    @Mock NotificationGateway notificationGateway;
    @InjectMocks KycServiceImpl service;

    @Test
    void uploadForbiddenWhenBorrowerNotRegisteredByOfficer() {
        Borrower b = Borrower.builder().borrowerId(1L).registeredByFieldOfficerId(2L).build();
        when(borrowerRepository.findById(1L)).thenReturn(Optional.of(b));

        // The "ref" argument has been removed to match the 4 required parameters:
        // (Long officerId, Long borrowerId, DocumentType documentType, MultipartFile file)
        assertThatThrownBy(() -> service.upload(999L, 1L, DocumentType.NATIONAL_ID, mock(MultipartFile.class)))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void getKycThrowsWhenMissing() {
        when(kycRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getKyc(9L)).isInstanceOf(ApiException.class);
    }
}