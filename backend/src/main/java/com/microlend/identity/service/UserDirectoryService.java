package com.microlend.identity.service;

import com.microlend.identity.dto.AdminUserResponse;
import com.microlend.identity.dto.CreateUserRequest;
import com.microlend.identity.dto.OfficerRosterResponse;
import com.microlend.identity.dto.UpdateUserRequest;

import java.util.List;

public interface UserDirectoryService {

    List<AdminUserResponse> listAllUsers();

    AdminUserResponse createUser(CreateUserRequest request);

    AdminUserResponse updateUser(Long userId, UpdateUserRequest request);

    void deleteUser(Long userId);

    List<OfficerRosterResponse> rosterForManager(Long managerUserId);
}
