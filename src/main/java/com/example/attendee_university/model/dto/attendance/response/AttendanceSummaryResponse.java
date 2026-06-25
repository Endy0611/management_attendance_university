package com.example.attendee_university.model.dto.attendance.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AttendanceSummaryResponse(

        UUID sessionId,

        long totalStudents,

        long present,

        long late,

        long absent
) {
}