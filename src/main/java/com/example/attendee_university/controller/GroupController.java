package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.group.request.AddGroupMembersRequest;
import com.example.attendee_university.model.dto.group.request.GroupRequest;
import com.example.attendee_university.model.dto.group.response.GroupMemberResponse;
import com.example.attendee_university.model.dto.group.response.GroupResponse;
import com.example.attendee_university.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;

    // ── Create group (Admin) ───────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@RequestBody @Valid GroupRequest request) {
        return ApiResponse.created("Group created.", groupService.createGroup(request));
    }

    // ── List all groups (Admin) ────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getAllGroups() {
        return ApiResponse.success("Groups fetched.", groupService.getAllGroups());
    }

    // ── Groups for the current user (any role) ─────────────────
    // ADMIN -> all groups | INSTRUCTOR -> groups they teach | STUDENT -> groups they're enrolled in
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getMyGroups() {
        return ApiResponse.success("Groups fetched.", groupService.getMyGroups());
    }

    // ── Get one group (Admin, or the owning Instructor) ────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable UUID id) {
        return ApiResponse.success("Group fetched.", groupService.getGroupById(id));
    }

    // ── Update group (Admin) ────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(@PathVariable UUID id, @RequestBody @Valid GroupRequest request) {
        return ApiResponse.success("Group updated.", groupService.updateGroup(id, request));
    }

    // ── Delete group (Admin) ────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ApiResponse.ok("Group deleted.");
    }

    // ── Add students to a group (Admin, or the owning Instructor) ──
    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> addMembers(
            @PathVariable UUID id, @RequestBody @Valid AddGroupMembersRequest request) {
        List<GroupMemberResponse> response = groupService.addMembers(id, request);
        return ApiResponse.success("Student(s) added to group.", response);
    }

    // ── Remove a student from a group (Admin, or the owning Instructor) ──
    @DeleteMapping("/{id}/members/{studentId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ApiResponse<Void> removeMember(@PathVariable UUID id, @PathVariable UUID studentId) {
        groupService.removeMember(id, studentId);
        return ApiResponse.ok("Student removed from group.");
    }

    // ── List members of a group (Admin, or the owning Instructor) ──
    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<GroupMemberResponse>>> getMembers(@PathVariable UUID id) {
        return ApiResponse.success("Members fetched.", groupService.getMembers(id));
    }
}