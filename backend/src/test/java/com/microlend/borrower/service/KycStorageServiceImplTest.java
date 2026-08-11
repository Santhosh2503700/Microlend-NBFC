package com.microlend.borrower.service;

import com.microlend.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KycStorageServiceImplTest {

    private final KycStorageService service =
            new KycStorageServiceImpl(System.getProperty("java.io.tmpdir") + "/microlend-kyc-test");

    @Test
    void storeRejectsEmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> service.store(file, 1L)).isInstanceOf(ApiException.class);
    }

    @Test
    void storeRejectsDisallowedContentTypeAndExtension() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/x-msdownload");
        when(file.getOriginalFilename()).thenReturn("malware.exe");
        assertThatThrownBy(() -> service.store(file, 1L)).isInstanceOf(ApiException.class);
    }
}
