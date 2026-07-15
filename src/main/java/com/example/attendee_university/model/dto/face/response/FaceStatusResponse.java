package com.example.attendee_university.model.dto.face.response;

import java.time.Instant;
import java.util.UUID;

public record FaceStatusResponse(
        UUID          userId,
        boolean       faceRegistered,
        Instant registeredAt    // null if not yet registered
) {}