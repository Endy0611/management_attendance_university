package com.example.attendee_university.model.dto.websocket;

import com.example.attendee_university.model.constraint.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private NotificationType type;
    private String title;
    private String message;
    private Instant timestamp;
}