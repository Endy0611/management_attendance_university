package com.example.attendee_university.model.dto.group.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GroupRequest(

        @NotNull(message = "Course is required")
        UUID courseId,

        @NotBlank(message = "Group name is required")
        String name,

        @NotNull(message = "Instructor is required")
        UUID instructorId,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,

        String semester
) {}