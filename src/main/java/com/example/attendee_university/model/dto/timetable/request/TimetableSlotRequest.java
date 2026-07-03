package com.example.attendee_university.model.dto.timetable.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotRequest(

        @NotNull(message = "Group is required")
        UUID groupId,

        @NotNull(message = "Zone (room) is required")
        UUID zoneId,

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        // if this date does not fall on dayOfWeek, the first generated session lands on the
        // next occurrence of dayOfWeek on/after this date
        @NotNull(message = "Valid-from date is required")
        LocalDate validFrom,

        @Min(value = 1, message = "Total sessions must be at least 1")
        int totalSessions
) {}
