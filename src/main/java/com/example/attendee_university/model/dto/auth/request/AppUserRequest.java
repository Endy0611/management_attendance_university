package com.example.attendee_university.model.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
public record AppUserRequest(

        @NotBlank(message = "Name is required")
        @Pattern(regexp = "^[\\p{L}\\s.'-]+$", message = "Name contains invalid characters")
        @Size(max = 100, message = "Name is too long")
        String name,

        @Email(message = "Invalid email")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Phone contains invalid characters")
        @Size(max = 20, message = "Phone number is too long")
        String phone
) {}