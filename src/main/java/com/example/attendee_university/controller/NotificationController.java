package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.notification.response.NotificationResponse;
import com.example.attendee_university.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    // ── My notifications ──────────────────────────────────────
    @Operation(summary = "Get all notifications for the current user (newest first)")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        return ApiResponse.success("Notifications fetched.", notificationService.getMyNotifications());
    }

    // ── Unread count ──────────────────────────────────────────
    @Operation(summary = "Get unread notification count (badge)")
    @GetMapping("/me/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        return ApiResponse.success("Unread count fetched.", notificationService.getMyUnreadCount());
    }

    // ── Mark all read ─────────────────────────────────────────
    @Operation(summary = "Mark all notifications as read")
    @PatchMapping("/me/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead() {
        notificationService.markAllRead();
        return ApiResponse.success("All notifications marked as read.", null);
    }
}