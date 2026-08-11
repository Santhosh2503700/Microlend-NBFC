package com.microlend.borrower.controller;

import com.microlend.borrower.dto.KycResponse;
import com.microlend.borrower.entity.BorrowerKYC;
import com.microlend.borrower.service.KycService;
import com.microlend.identity.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/credit-officer/kyc")
@RequiredArgsConstructor
public class CreditOfficerKycController {

    private final KycService kycService;

    @PutMapping("/{kycId}/verify")
    public KycResponse verify(@PathVariable Long kycId, @RequestBody Map<String, Object> body) {
        boolean approve = "VERIFIED".equalsIgnoreCase(String.valueOf(body.getOrDefault("status", "VERIFIED")))
                || Boolean.TRUE.equals(body.get("approve"));
        String remarks = body.get("remarks") == null ? null : String.valueOf(body.get("remarks"));
        return kycService.verify(SecurityUtil.currentUserId(), kycId, approve, remarks);
    }

    @GetMapping("/{kycId}/file")
    public ResponseEntity<Resource> file(@PathVariable Long kycId) {
        BorrowerKYC kyc = kycService.getKyc(kycId);
        Path path = kycService.resolveFile(kycId);
        Resource resource = new FileSystemResource(path);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
                .body(resource);
    }
}
