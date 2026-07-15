package com.example.attendee_university.model.dto.attendance.response;

import com.example.attendee_university.model.constraint.AttendanceStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AttendanceResponse(

        UUID attendanceId,

        UUID sessionId,

        UUID studentId,

        String studentName,

        Instant checkedInAt,

        Double latitude,

        Double longitude,

        Double distanceMeters,

        AttendanceStatus status
) {
}