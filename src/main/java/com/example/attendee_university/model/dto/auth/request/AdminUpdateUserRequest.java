package com.example.attendee_university.model.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 100)
        String name,

        String phone,

        String studentId,

        Integer generation,

        String avatar
) {}