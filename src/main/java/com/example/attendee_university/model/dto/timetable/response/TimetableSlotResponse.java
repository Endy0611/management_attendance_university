package com.example.attendee_university.model.dto.timetable.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimetableSlotResponse {
    private UUID id;
    private UUID groupId;
    private String groupName;
    private String courseCode;
    private String instructorName;
    private UUID zoneId;
    private String zoneName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate validFrom;
    private int totalSessions;
    private int generatedSessionsCount;
    private Instant createdAt;
}
