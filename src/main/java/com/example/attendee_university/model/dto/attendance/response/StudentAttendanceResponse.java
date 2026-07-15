package com.example.attendee_university.model.dto.attendance.response;

import com.example.attendee_university.model.constraint.AttendanceStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record StudentAttendanceResponse(

        UUID attendanceId,

        UUID sessionId,

        String sessionTitle,

        String groupName,

        String courseCode,

        AttendanceStatus status,

        Instant checkedInAt
) {
}