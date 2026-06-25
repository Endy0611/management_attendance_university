package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.attendance.request.AttendanceCheckInRequest;
import com.example.attendee_university.model.dto.attendance.response.AbsentStudentResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceSummaryResponse;
import com.example.attendee_university.model.dto.attendance.response.StudentAttendanceResponse;
import com.example.attendee_university.service.AttendanceService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/attendance")
@SecurityRequirement(name = "bearerAuth")
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ── Student checks in ─────────────────────────────────────
    @Operation(summary = "Check in to an active session (STUDENT)")
    @PostMapping("/sessions/{sessionId}/check-in")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @PathVariable UUID sessionId,
            @RequestBody @Valid AttendanceCheckInRequest request) {
        AttendanceResponse response = attendanceService.checkIn(sessionId, request);
        return ApiResponse.success("Checked in successfully.", response);
    }

    // ── Attendance list for a session ─────────────────────────
    @Operation(summary = "Get all attendance records for a session (ADMIN / INSTRUCTOR)")
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public List<AttendanceResponse> getSessionAttendance(@PathVariable UUID sessionId) {
        return attendanceService.getSessionAttendance(sessionId);
    }

    // ── Absent students list ──────────────────────────────────
    @Operation(summary = "Get students who did NOT check in for a session (ADMIN / INSTRUCTOR)")
    @GetMapping("/sessions/{sessionId}/absent")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public List<AbsentStudentResponse> getAbsentStudents(@PathVariable UUID sessionId) {
        return attendanceService.getAbsentStudents(sessionId);
    }

    // ── Attendance summary ────────────────────────────────────
    @Operation(summary = "Get attendance summary — present / late / absent counts (ADMIN / INSTRUCTOR)")
    @GetMapping("/sessions/{sessionId}/summary")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AttendanceSummaryResponse>> getSummary(
            @PathVariable UUID sessionId) {
        return ApiResponse.success("Summary fetched.", attendanceService.getSessionSummary(sessionId));
    }

    // ── Manual override ───────────────────────────────────────
    @Operation(summary = "Manually set a student's attendance status (ADMIN / INSTRUCTOR)")
    @PatchMapping("/sessions/{sessionId}/students/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> manualOverride(
            @PathVariable UUID sessionId,
            @PathVariable UUID studentId,
            @RequestParam String status) {
        AttendanceResponse response = attendanceService.manualOverride(sessionId, studentId, status);
        return ApiResponse.success("Attendance status updated.", response);
    }

    // ── My attendance history (STUDENT) ───────────────────────
    @Operation(summary = "Get my attendance history (STUDENT)")
    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<StudentAttendanceResponse> getMyAttendance() {
        return attendanceService.getMyAttendance();
    }

    // ── All past sessions of my enrolled groups (STUDENT) ─────
    @Operation(summary = "Get all past sessions for my enrolled groups with attendance status (STUDENT)")
    @GetMapping("/me/sessions")
    @PreAuthorize("hasAuthority('STUDENT')")
    public List<StudentAttendanceResponse> getMySessionHistory() {
        return attendanceService.getSessionHistoryForMyGroups();
    }
}