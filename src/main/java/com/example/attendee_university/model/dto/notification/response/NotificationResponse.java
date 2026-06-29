package com.example.attendee_university.model.dto.notification.response;

import com.example.attendee_university.model.constraint.NotificationType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record NotificationResponse(
        UUID             id,
        NotificationType type,
        String           title,
        String           message,
        boolean          read,
        LocalDateTime    createdAt
) {}