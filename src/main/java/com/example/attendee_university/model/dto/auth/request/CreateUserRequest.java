package com.example.attendee_university.model.dto.auth.request;

import com.example.attendee_university.model.constraint.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        String studentId,
        Integer generation,
        @NotNull RoleType role
) {}