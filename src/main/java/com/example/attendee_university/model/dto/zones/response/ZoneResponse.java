package com.example.attendee_university.model.dto.zones.response;

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
public class ZoneResponse {
    private UUID id;
    private String name;
    private double latitude;
    private double longitude;
    private double radiusMeters;
    private UUID createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}