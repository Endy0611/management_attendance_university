package com.example.attendee_university.model.dto.attendance.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AttendanceCheckInRequest(

        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        // raw device fingerprint string, same value sent during /devices/bind
        @NotBlank
        String deviceFingerprint,

        // base64-encoded JPEG/PNG frame captured client-side, no data: prefix
        @NotBlank
        String faceImageBase64
) {
}