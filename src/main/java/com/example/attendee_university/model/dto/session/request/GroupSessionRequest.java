package com.example.attendee_university.model.dto.session.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record GroupSessionRequest(

        @NotNull(message = "Group is required")
        UUID groupId,

        @NotNull(message = "Zone is required")
        UUID zoneId,

        @NotNull(message = "Start time is required")
        Instant startTime,

        @NotNull(message = "End time is required")
        Instant endTime
) {}