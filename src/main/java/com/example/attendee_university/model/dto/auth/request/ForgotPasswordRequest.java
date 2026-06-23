package com.example.attendee_university.model.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @NotNull
        @Email(message = "Invalid email format")
        @Pattern(
                regexp = "^[a-z0-9._%+-]+@gmail\\.com$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Only Gmail addresses are allowed")
        @Schema(example = "your_email@gmail.com")
        String email) {}
