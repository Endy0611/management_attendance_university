package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.zones.request.ZoneRequest;
import com.example.attendee_university.model.dto.zones.response.ZoneResponse;
import com.example.attendee_university.service.ZoneService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ZoneController {

    private final ZoneService zoneService;

    // ── Create zone (Admin) ────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ZoneResponse>> createZone(
            @RequestBody @Valid ZoneRequest request) {
        ZoneResponse response = zoneService.createZone(request);
        return ApiResponse.success("Zone created.", response);
    }

    // ── List all zones (Admin, Instructor) ─────────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public List<ZoneResponse> getAllZones() {
        return zoneService.getAllZones();
    }

    // ── Get one zone ───────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ZoneResponse getZone(@PathVariable UUID id) {
        return zoneService.getZoneById(id);
    }

    // ── Update zone (Admin) ────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ZoneResponse updateZone(
            @PathVariable UUID id,
            @RequestBody @Valid ZoneRequest request) {
        return zoneService.updateZone(id, request);
    }

    // ── Delete zone (Admin) ────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteZone(@PathVariable UUID id) {
        zoneService.deleteZone(id);
        return ApiResponse.ok("Zone deleted.");
    }
}