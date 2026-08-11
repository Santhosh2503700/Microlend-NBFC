package com.microlend.identity.service;

import com.microlend.identity.dto.UpdateProfileRequest;
import com.microlend.identity.dto.UserProfileResponse;


public interface UserProfileService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
}
