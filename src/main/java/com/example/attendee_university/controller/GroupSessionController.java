package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.session.request.GroupSessionRequest;
import com.example.attendee_university.model.dto.session.response.GroupSessionResponse;
import com.example.attendee_university.service.GroupSessionService;
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
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GroupSessionController {

    private final GroupSessionService groupSessionService;

    // ── Create session (Admin, Instructor) ─────────────────────
    @Operation(summary = "Create a session for a group in a zone")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<GroupSessionResponse>> createSession(
            @RequestBody @Valid GroupSessionRequest request) {
        GroupSessionResponse response = groupSessionService.createSession(request);
        return ApiResponse.success("Session created.", response);
    }

    // ── List all sessions (Admin) ──────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<GroupSessionResponse> getAllSessions() {
        return groupSessionService.getAllSessions();
    }

    // ── Sessions for a specific group (Admin, Instructor) ──────
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public List<GroupSessionResponse> getSessionsByGroup(@PathVariable UUID groupId) {
        return groupSessionService.getSessionsByGroup(groupId);
    }

    // ── Get one session (Admin, Instructor) ────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public GroupSessionResponse getSession(@PathVariable UUID id) {
        return groupSessionService.getSessionById(id);
    }

    // ── Update session (Admin, Instructor) ─────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public GroupSessionResponse updateSession(
            @PathVariable UUID id,
            @RequestBody @Valid GroupSessionRequest request) {
        return groupSessionService.updateSession(id, request);
    }

    // ── Delete session (Admin) ─────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteSession(@PathVariable UUID id) {
        groupSessionService.deleteSession(id);
        return ApiResponse.ok("Session deleted.");
    }

    // ── Active sessions for current user (all roles) ───────────
    @Operation(summary = "Get currently active sessions for my groups")
    @GetMapping("/me/active")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> getMyActiveSessions() {
        return ApiResponse.success("Active sessions fetched.", groupSessionService.getMyActiveSessions());
    }

    // ── Past sessions for current student's enrolled groups ────
    @Operation(summary = "Get all past (ended) sessions for my enrolled groups (STUDENT)")
    @GetMapping("/me/history")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<ApiResponse<List<GroupSessionResponse>>> getMySessionHistory() {
        return ApiResponse.success("Session history fetched.", groupSessionService.getPastSessionsForMyGroups());
    }
}