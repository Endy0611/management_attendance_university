package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.face.request.FaceRegisterRequest;
import com.example.attendee_university.model.dto.face.request.FaceVerifyRequest;
import com.example.attendee_university.model.dto.face.response.FaceStatusResponse;
import com.example.attendee_university.model.dto.face.response.FaceVerifyResponse;
import com.example.attendee_university.service.FaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/faces")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FaceController {

    private final FaceService faceService;

    // ── Register face (STUDENT) ───────────────────────────────
    @Operation(summary = "Register face — send base64 image to store embedding")
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ApiResponse<FaceStatusResponse>> register(
            @RequestBody @Valid FaceRegisterRequest request) {
        FaceStatusResponse response = faceService.registerFace(request);
        return ApiResponse.success("Face registered successfully.", response);
    }

    // ── Verify face (STUDENT) ─────────────────────────────────
    @Operation(summary = "Verify face against stored embedding")
    @PostMapping("/verify")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ApiResponse<FaceVerifyResponse>> verify(
            @RequestBody @Valid FaceVerifyRequest request) {
        FaceVerifyResponse response = faceService.verifyFace(request);
        return ApiResponse.success("Face verification completed.", response);
    }

    // ── Get my face status ────────────────────────────────────
    @Operation(summary = "Get current user face registration status")
    @GetMapping("/me/status")
    public ResponseEntity<ApiResponse<FaceStatusResponse>> getMyStatus() {
        return ApiResponse.success("Face status fetched.", faceService.getMyFaceStatus());
    }

    // ── ADMIN: Reset face ─────────────────────────────────────
    @Operation(summary = "ADMIN — Reset a student's face so they can re-register")
    @DeleteMapping("/admin/{userId}/reset")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> adminReset(@PathVariable UUID userId) {
        faceService.adminResetFace(userId);
        return ApiResponse.success("Face reset. Student can re-register.", null);
    }
}