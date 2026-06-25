package com.example.attendee_university.model.dto.websocket;

import com.example.attendee_university.model.constraint.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusEvent {

    private UUID sessionId;
    private String groupName;
    private String zoneName;
    private SessionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime eventTime;
}