package com.microlend.identity.controller;

import com.microlend.identity.dto.UpdateProfileRequest;
import com.microlend.identity.dto.UserProfileResponse;
import com.microlend.identity.security.SecurityUtil;
import com.microlend.identity.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(userProfileService.getProfile(userId));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtil.currentUserId();
        return ResponseEntity.ok(userProfileService.updateProfile(userId, request));
    }
}