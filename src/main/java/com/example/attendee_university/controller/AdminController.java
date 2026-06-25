package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.auth.request.AdminUpdateUserRequest;
import com.example.attendee_university.model.dto.auth.request.CreateUserRequest;
import com.example.attendee_university.model.dto.auth.response.AppUserResponse;
import com.example.attendee_university.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AppUserService appUserService;

    // ── Create student or instructor account ──────────────────
    @Operation(summary = "Create a student or instructor account")
    @PostMapping("/users")
    public AppUserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return appUserService.createUser(request);
    }

    // ── List all users ────────────────────────────────────────
    @Operation(summary = "List all users")
    @GetMapping("/users")
    public List<AppUserResponse> listUsers() {
        return appUserService.getAllUsers();
    }

    // ── Get single user ───────────────────────────────────────
    @Operation(summary = "Get a single user by ID")
    @GetMapping("/users/{id}")
    public AppUserResponse getUser(@PathVariable UUID id) {
        return appUserService.getUserById(id);
    }

    // ── Update user info ──────────────────────────────────────
    @Operation(summary = "Update user info (name, phone, studentId, generation, avatar)")
    @PutMapping("/users/{id}")
    public AppUserResponse updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid AdminUpdateUserRequest request) {
        return appUserService.adminUpdateUser(id, request);
    }

    // ── Change role ───────────────────────────────────────────
    @Operation(summary = "Change user role (ADMIN / INSTRUCTOR / STUDENT)")
    @PatchMapping("/users/{id}/role")
    public ApiResponse<Void> changeRole(@PathVariable UUID id, @RequestParam String role) {
        appUserService.changeRole(id, role);
        return ApiResponse.ok("Role updated.");
    }

    // ── Reset user password ───────────────────────────────────
    @Operation(summary = "Reset user password — sends temp password via email")
    @PostMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable UUID id) {
        appUserService.adminResetPassword(id);
        return ApiResponse.ok("Password reset and sent to user email.");
    }

    // ── Ban / unban user ──────────────────────────────────────
    @Operation(summary = "Activate or deactivate (ban) a user account")
    @PatchMapping("/users/{id}/active")
    public ApiResponse<Void> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        appUserService.setUserActive(id, active);
        return ApiResponse.ok(active ? "User activated." : "User deactivated.");
    }

    // ── Reset device binding ──────────────────────────────────
    @Operation(summary = "Reset device binding so the student can bind a new device")
    @PatchMapping("/users/{id}/device-reset")
    public ApiResponse<Void> resetDevice(@PathVariable UUID id) {
        appUserService.adminResetDevice(id);
        return ApiResponse.ok("Device binding reset. Student can now bind a new device.");
    }
}