package com.microlend.identity.controller;

import com.microlend.identity.dto.AdminUserResponse;
import com.microlend.identity.dto.CreateUserRequest;
import com.microlend.identity.dto.UpdateUserRequest;
import com.microlend.identity.service.UserDirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserDirectoryService userDirectoryService;

    @GetMapping
    public List<AdminUserResponse> listUsers() {
        return userDirectoryService.listAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userDirectoryService.createUser(request);
    }

    @PutMapping("/{id}")
    public AdminUserResponse updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userDirectoryService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("id") Long id) {
        userDirectoryService.deleteUser(id);
    }
}