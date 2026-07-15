package com.example.attendee_university.service.impl;

import com.example.attendee_university.model.constraint.NotificationType;
import com.example.attendee_university.model.dto.notification.response.NotificationResponse;
import com.example.attendee_university.model.dto.websocket.NotificationEvent;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.NotificationRecord;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.NotificationRepository;
import com.example.attendee_university.service.NotificationService;
import com.example.attendee_university.utils.HandleCurrentUser;
import com.example.attendee_university.websocket.handler.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AppUserRepository      appUserRepository;
    private final HandleCurrentUser      handleCurrentUser;
    private final WebSocketPublisher     wsPublisher;

    // ── Save to DB + push via WebSocket ──────────────────────
    @Override
    @Transactional
    public void send(UUID userId, NotificationType type, String title, String message) {
        NotificationRecord record = NotificationRecord.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .build();
        notificationRepository.save(record);

        appUserRepository.findById(userId).ifPresent(user ->
                wsPublisher.notifyUser(user.getEmail(), NotificationEvent.builder()
                        .type(type)
                        .title(title)
                        .message(message)
                        .timestamp(Instant.now())
                        .build())
        );

        log.debug("Notification sent to userId={} type={}", userId, type);
    }

    // ── My notification history ───────────────────────────────
    @Override
    public List<NotificationResponse> getMyNotifications() {
        AppUser user = handleCurrentUser.getCurrentUser();
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Unread count ──────────────────────────────────────────
    @Override
    public long getMyUnreadCount() {
        AppUser user = handleCurrentUser.getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    // ── Mark all read ─────────────────────────────────────────
    @Override
    @Transactional
    public void markAllRead() {
        AppUser user = handleCurrentUser.getCurrentUser();
        notificationRepository.markAllReadByUserId(user.getId());
    }

    private NotificationResponse toResponse(NotificationRecord r) {
        return NotificationResponse.builder()
                .id(r.getId())
                .type(r.getType())
                .title(r.getTitle())
                .message(r.getMessage())
                .read(r.isRead())
                .createdAt(r.getCreatedAt())
                .build();
    }
}