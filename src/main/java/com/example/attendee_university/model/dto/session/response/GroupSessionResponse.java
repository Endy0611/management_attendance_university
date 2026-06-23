package com.example.attendee_university.model.dto.session.response;

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
public class GroupSessionResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String courseCode;
    private UUID          zoneId;
    private String        zoneName;
    private double         latitude;
    private double         longitude;
    private double         radiusMeters;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean        active;
    private LocalDateTime createdAt;
}