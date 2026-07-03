package com.example.attendee_university.model.dto.major.request;

import jakarta.validation.constraints.NotBlank;

public record MajorRequest(

        @NotBlank(message = "Major name is required")
        String name,

        @NotBlank(message = "Major code is required")
        String code
) {}