package com.example.attendee_university.model.dto.session.response;

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
public class GroupSessionResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String courseCode;
    private UUID zoneId;
    private String zoneName;
    private double latitude;
    private double longitude;
    private double radiusMeters;
    private Instant startTime;
    private Instant endTime;
    private boolean active;
    private boolean alreadyCheckedIn;
    private Instant createdAt;
}