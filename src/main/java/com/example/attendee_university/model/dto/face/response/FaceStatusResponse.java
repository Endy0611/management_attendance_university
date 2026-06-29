package com.example.attendee_university.model.dto.face.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FaceStatusResponse(
        UUID          userId,
        boolean       faceRegistered,
        LocalDateTime registeredAt    // null if not yet registered
) {}