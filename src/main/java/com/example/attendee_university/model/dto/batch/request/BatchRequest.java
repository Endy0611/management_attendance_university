package com.example.attendee_university.model.dto.batch.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record BatchRequest(

        @NotBlank(message = "Batch name is required")
        String name,

        @Min(value = 2000, message = "Intake year looks invalid")
        int intakeYear
) {}