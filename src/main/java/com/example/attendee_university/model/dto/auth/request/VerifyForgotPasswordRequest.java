package com.example.attendee_university.model.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record VerifyForgotPasswordRequest(
        @NotBlank(message = "Email is required")
        @NotNull
        @Email(message = "Invalid email format")
        @Pattern(
                regexp = "^[a-z0-9._%+-]+@gmail\\.com$",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Only Gmail addresses are allowed")
        @Schema(example = "your_email@gmail.com")
        String email,
        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
        @Pattern(regexp = "^[0-9]+$", message = "OTP must contain only numbers")
        String otp) {}
