package com.microlend.identity.service;

import com.microlend.borrower.repository.BorrowerRepository;
import com.microlend.delinquency.enums.CaseStatus;
import com.microlend.delinquency.repository.DelinquencyCaseRepository;
import com.microlend.identity.dto.AdminUserResponse;
import com.microlend.identity.dto.CreateUserRequest;
import com.microlend.identity.dto.OfficerRosterResponse;
import com.microlend.identity.dto.UpdateUserRequest;
import com.microlend.identity.enums.Role;
import com.microlend.identity.entity.User;
import com.microlend.identity.enums.UserStatus;
import com.microlend.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserDirectoryServiceImpl implements UserDirectoryService {

    private final UserRepository userRepository;
    private final BorrowerRepository borrowerRepository;
    private final DelinquencyCaseRepository delinquencyCaseRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUserId))
                .map(AdminUserResponse::from)
                .toList();
    }

    /** Provision a new portal account from the NBFC Admin console (requires valid phone number). */
    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists.");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(Role.valueOf(request.role()));
        user.setBranchId(request.branchId());

        String rawPassword = (request.password() != null && !request.password().trim().isEmpty())
                ? request.password()
                : "Password@123";

        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setStatus(UserStatus.ACTIVE);
        user.setMustResetPassword(true);

        User savedUser = userRepository.save(user);
        return AdminUserResponse.from(savedUser);
    }

    /** Update an existing user's details without requiring phone number input. */
    @Transactional
    public AdminUserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id " + userId));

        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(Role.valueOf(request.role()));
        user.setBranchId(request.branchId());

        User updatedUser = userRepository.save(user);
        return AdminUserResponse.from(updatedUser);
    }

    /** Delete a user account by ID. */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id " + userId);
        }
        userRepository.deleteById(userId);
    }

    /** Field Officers + Collections Officers in the manager's branch, each with a workload count. */
    @Transactional(readOnly = true)
    public List<OfficerRosterResponse> rosterForManager(Long managerUserId) {
        Long branchId = userRepository.findById(managerUserId).map(User::getBranchId).orElse(null);
        if (branchId == null) {
            return List.of();
        }
        List<OfficerRosterResponse> roster = new ArrayList<>();
        for (User fo : userRepository.findByBranchIdAndRole(branchId, Role.FIELD_OFFICER)) {
            long borrowers = borrowerRepository.findByRegisteredByFieldOfficerId(fo.getUserId()).size();
            roster.add(new OfficerRosterResponse(fo.getUserId(), fo.getName(), fo.getEmail(),
                    fo.getRole(), fo.getBranchId(), fo.getStatus(), borrowers, "Borrowers registered"));
        }
        for (User co : userRepository.findByBranchIdAndRole(branchId, Role.COLLECTIONS_OFFICER)) {
            long openCases = delinquencyCaseRepository
                    .findByAssignedCollectionsOfficerIdAndStatusNot(co.getUserId(), CaseStatus.RESOLVED).size();
            roster.add(new OfficerRosterResponse(co.getUserId(), co.getName(), co.getEmail(),
                    co.getRole(), co.getBranchId(), co.getStatus(), openCases, "Open cases assigned"));
        }
        return roster;
    }
}