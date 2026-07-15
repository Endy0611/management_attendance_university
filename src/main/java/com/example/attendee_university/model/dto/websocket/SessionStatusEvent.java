package com.example.attendee_university.model.dto.websocket;

import com.example.attendee_university.model.constraint.SessionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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
    private Instant startTime;
    private Instant endTime;
    private Instant eventTime;
}