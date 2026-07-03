package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.major.request.MajorRequest;
import com.example.attendee_university.model.dto.major.response.MajorResponse;
import com.example.attendee_university.service.MajorService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class MajorController {

    private final MajorService majorService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<MajorResponse>> createMajor(@RequestBody @Valid MajorRequest request) {
        return ApiResponse.created("Major created.", majorService.createMajor(request));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MajorResponse>>> getAllMajors() {
        return ApiResponse.success("Majors fetched.", majorService.getAllMajors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MajorResponse>> getMajor(@PathVariable UUID id) {
        return ApiResponse.success("Major fetched.", majorService.getMajorById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<MajorResponse>> updateMajor(@PathVariable UUID id, @RequestBody @Valid MajorRequest request) {
        return ApiResponse.success("Major updated.", majorService.updateMajor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteMajor(@PathVariable UUID id) {
        majorService.deleteMajor(id);
        return ApiResponse.ok("Major deleted.");
    }
}