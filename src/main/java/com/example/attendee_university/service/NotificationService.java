package com.example.attendee_university.service;

import com.example.attendee_university.model.constraint.NotificationType;
import com.example.attendee_university.model.dto.notification.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    /** Save to DB + push via WebSocket. Replace direct wsPublisher calls with this. */
    void send(UUID userId, NotificationType type, String title, String message);

    List<NotificationResponse> getMyNotifications();

    long getMyUnreadCount();

    void markAllRead();
}