package com.example.attendee_university.model.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AppUserUpdateRequest(

        @NotBlank(message = "Name is required")
        @Pattern(regexp = "^[\\p{L}\\s.'-]+$", message = "Name contains invalid characters")
        @Size(max = 100, message = "Name must not exceed 100 characters")
        String name,

        @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Phone contains invalid characters")
        @Size(max = 20, message = "Phone number is too long")
        String phone,

        @Pattern(regexp = "^[a-zA-Z0-9._/\\-]*$", message = "Avatar reference is invalid")
        @Size(max = 255, message = "Avatar value is too long")
        String avatar
) {}