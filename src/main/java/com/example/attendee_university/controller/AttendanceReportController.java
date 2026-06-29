package com.example.attendee_university.controller;

import com.example.attendee_university.service.AttendanceReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
public class AttendanceReportController {

    private final AttendanceReportService reportService;

    // ── Export single session CSV ─────────────────────────────
    @Operation(summary = "Export attendance for a session as CSV (ADMIN / INSTRUCTOR)")
    @GetMapping("/sessions/{sessionId}/export")
    public ResponseEntity<Resource> exportSession(@PathVariable UUID sessionId) {
        byte[] csv = reportService.exportSessionCsv(sessionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-session-" + sessionId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ByteArrayResource(csv));
    }

    // ── Export full group CSV matrix ──────────────────────────
    @Operation(summary = "Export attendance for an entire group as CSV (ADMIN / INSTRUCTOR)")
    @GetMapping("/groups/{groupId}/export")
    public ResponseEntity<Resource> exportGroup(@PathVariable UUID groupId) {
        byte[] csv = reportService.exportGroupCsv(groupId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"attendance-group-" + groupId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new ByteArrayResource(csv));
    }
}