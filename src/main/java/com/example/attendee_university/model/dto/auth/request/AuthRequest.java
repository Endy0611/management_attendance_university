package com.example.attendee_university.model.dto.auth.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(

        @NotBlank(message = "Email is required")
        String identifier,   // email

        @NotBlank(message = "Password is required")
        String password,

        Boolean rememberMe
) {}