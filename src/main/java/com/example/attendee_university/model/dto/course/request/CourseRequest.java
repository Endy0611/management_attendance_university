package com.example.attendee_university.model.dto.course.request;

import jakarta.validation.constraints.NotBlank;

public record CourseRequest(

        @NotBlank(message = "Course code is required")
        String code,

        @NotBlank(message = "Course name is required")
        String name
) {}