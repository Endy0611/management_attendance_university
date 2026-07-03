package com.example.attendee_university.model.dto.group.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GroupRequest(

        // Legacy single-course/instructor link — kept required for backward
        // compatibility with the already-shipped frontend. New code should
        // treat TimetableSlot as the source of truth for what a group
        // actually studies; these two are the group's nominal default.
        @NotNull(message = "Course is required")
        UUID courseId,

        @NotBlank(message = "Group name is required")
        String name,

        @NotNull(message = "Instructor is required")
        UUID instructorId,

        @Min(value = 1, message = "Capacity must be at least 1")
        int capacity,

        String semester,

        // New hierarchy fields — optional for now so existing callers that
        // don't send them yet don't break. Becomes required once the
        // frontend migrates onto Batch/Major/Shift pickers.
        UUID batchId,

        UUID majorId,

        String shift
) {}