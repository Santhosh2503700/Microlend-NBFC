package com.microlend.identity.service;

import com.microlend.borrower.entity.Borrower;
import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.common.ApiException;
import com.microlend.identity.dto.UpdateProfileRequest;
import com.microlend.identity.dto.UserProfileResponse;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final BorrowerRepository borrowerRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .phone(user.getPhone())
                .branchId(user.getBranchId());

        if (user.getRole() == Role.BORROWER) {
            borrowerRepository.findByPortalUserId(user.getUserId())
                    .ifPresent(b -> {
                        builder.phone(b.getPhone() != null ? b.getPhone() : user.getPhone());
                        builder.bankAccountNumber(b.getBankAccountNumber());
                        builder.ifscCode(b.getIfscCode());
                    });
        }

        return builder.build();
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }

        userRepository.save(user);

        if (user.getRole() == Role.BORROWER) {
            Borrower borrower = borrowerRepository.findByPortalUserId(user.getUserId())
                    .orElse(null);

            if (borrower != null) {
                if (request.getName() != null && !request.getName().trim().isEmpty()) {
                    borrower.setName(request.getName().trim());
                }
                if (request.getPhone() != null) {
                    borrower.setPhone(request.getPhone().trim());
                }
                if (request.getBankAccountNumber() != null && !request.getBankAccountNumber().trim().isEmpty()) {
                    borrower.setBankAccountNumber(request.getBankAccountNumber().trim());
                }
                if (request.getIfscCode() != null && !request.getIfscCode().trim().isEmpty()) {
                    borrower.setIfscCode(request.getIfscCode().trim().toUpperCase());
                }
                borrowerRepository.save(borrower);
            }
        }

        return getProfile(userId);
    }
}