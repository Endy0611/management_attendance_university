package com.example.attendee_university.model.dto.holiday.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayRequest(

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotBlank(message = "Holiday name is required")
        String name
) {}