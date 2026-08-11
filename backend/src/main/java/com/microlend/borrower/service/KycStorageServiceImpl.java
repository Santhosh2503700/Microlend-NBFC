package com.microlend.borrower.service;

import com.microlend.common.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;


@Service
@Slf4j
public class KycStorageServiceImpl implements KycStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("application/pdf", "image/jpeg", "image/jpg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");

    private final Path root;

    public KycStorageServiceImpl(@Value("${microlend.storage.kyc-upload-dir:./uploads/kyc}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
            log.info("KYC storage root: {}", root);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create KYC storage directory: " + root, e);
        }
    }

    // Stores the uploaded file and returns a served URL path
    public String store(MultipartFile file, Long borrowerId) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }
        String contentType = file.getContentType();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        ext = ext == null ? "" : ext.toLowerCase();
        if ((contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase()))
                && !ALLOWED_EXTENSIONS.contains(ext)) {
            throw ApiException.badRequest("Only PDF, JPG or PNG files are allowed");
        }
        String filename = "kyc_" + borrowerId + "_" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw ApiException.badRequest("Invalid file path");
        }
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApiException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file");
        }
        return "/uploads/kyc/" + filename;
    }

    // Resolves a stored URL path back to a filesystem path for serving.
    public Path resolve(String storedUrl) {
        String filename = storedUrl.substring(storedUrl.lastIndexOf('/') + 1);
        Path p = root.resolve(filename).normalize();
        if (!p.startsWith(root) || !Files.exists(p)) {
            throw ApiException.notFound("File not found");
        }
        return p;
    }
}
