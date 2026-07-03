package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.timetable.request.TimetableSlotRequest;
import com.example.attendee_university.model.dto.timetable.response.TimetableSlotResponse;
import com.example.attendee_university.service.TimetableSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/timetable-slots")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TimetableSlotController {

    private final TimetableSlotService timetableSlotService;

    // ── Create a recurring weekly slot (Admin, or the owning Instructor) ──
    @Operation(summary = "Create a recurring weekly slot for a group; auto-generates its dated sessions")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<TimetableSlotResponse>> createSlot(
            @RequestBody @Valid TimetableSlotRequest request) {
        TimetableSlotResponse response = timetableSlotService.createSlot(request);
        return ApiResponse.created("Timetable slot created.", response);
    }

    // ── Get one slot ─────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public TimetableSlotResponse getSlot(@PathVariable UUID id) {
        return timetableSlotService.getSlotById(id);
    }

    // ── Full weekly timetable for a batch/group (any role) ──────
    @Operation(summary = "Get the weekly timetable for a group/batch, Monday to Sunday")
    @GetMapping("/group/{groupId}")
    public List<TimetableSlotResponse> getWeeklyTimetableForGroup(@PathVariable UUID groupId) {
        return timetableSlotService.getWeeklyTimetableForGroup(groupId);
    }

    // ── My weekly timetable (role-aware, any role) ───────────────
    @Operation(summary = "Get my weekly timetable (role-aware: admin/instructor/student)")
    @GetMapping("/me")
    public List<TimetableSlotResponse> getMyWeeklyTimetable() {
        return timetableSlotService.getMyWeeklyTimetable();
    }

    // ── Update slot (Admin, or the owning Instructor) ────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public TimetableSlotResponse updateSlot(@PathVariable UUID id, @RequestBody @Valid TimetableSlotRequest request) {
        return timetableSlotService.updateSlot(id, request);
    }

    // ── Delete slot (Admin, or the owning Instructor) ────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ApiResponse<Void> deleteSlot(@PathVariable UUID id) {
        timetableSlotService.deleteSlot(id);
        return ApiResponse.ok("Timetable slot deleted.");
    }

    // ── Manually (re)generate sessions for a slot ────────────────
    @Operation(summary = "(Re)generate the dated sessions for a slot; idempotent")
    @PostMapping("/{id}/generate-sessions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Integer>> generateSessions(@PathVariable UUID id) {
        int generated = timetableSlotService.generateSessions(id);
        return ApiResponse.success(generated + " session(s) generated.", generated);
    }
}
