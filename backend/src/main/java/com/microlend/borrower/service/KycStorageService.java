package com.microlend.borrower.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;


public interface KycStorageService {

    String store(MultipartFile file, Long borrowerId);

    Path resolve(String storedUrl);
}
