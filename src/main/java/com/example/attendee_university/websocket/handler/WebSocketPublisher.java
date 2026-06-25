package com.example.attendee_university.websocket.handler;

import com.example.attendee_university.model.dto.websocket.AttendanceEvent;
import com.example.attendee_university.model.dto.websocket.NotificationEvent;
import com.example.attendee_university.model.dto.websocket.SessionStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishAttendance(UUID sessionId, AttendanceEvent event) {
        String destination = "/topic/sessions/" + sessionId + "/attendance";
        messagingTemplate.convertAndSend(destination, event);
        log.debug("[WS] Attendance event sent to {}: student={}", destination, event.getStudentId());
    }

    // ── Session status change → all subscribers of a session ────────────────
    public void publishSessionStatus(UUID sessionId, SessionStatusEvent event) {
        String destination = "/topic/sessions/" + sessionId + "/status";
        messagingTemplate.convertAndSend(destination, event);
        log.info("[WS] Session status '{}' sent to {}", event.getStatus(), destination);
    }

    // ── Private notification → a specific user ──────────────────────────────
    // userEmail must match the principal name set by JwtHandshakeInterceptor.
    public void notifyUser(String userEmail, NotificationEvent event) {
        messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", event);
        log.debug("[WS] Notification '{}' sent to user {}", event.getType(), userEmail);
    }
}