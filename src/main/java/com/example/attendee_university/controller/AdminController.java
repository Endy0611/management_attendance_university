package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.auth.request.CreateUserRequest;
import com.example.attendee_university.model.dto.auth.response.AppUserResponse;
import com.example.attendee_university.service.AppUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AppUserService appUserService;

    // ── Create student or instructor account ──────────────────
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public AppUserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return appUserService.createUser(request);
    }

    // ── Reset user password ───────────────────────────────────
    @PostMapping("/users/{id}/reset-password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> resetPassword(@PathVariable UUID id) {
        appUserService.adminResetPassword(id);
        return ApiResponse.ok("Password reset and sent to user email.");
    }

    // ── Change role ───────────────────────────────────────────
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> changeRole(@PathVariable UUID id, @RequestParam String role) {
        appUserService.changeRole(id, role);
        return ApiResponse.ok("Role updated.");
    }

    // ── List all users ────────────────────────────────────────
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public java.util.List<AppUserResponse> listUsers() {
        return appUserService.getAllUsers();
    }
}