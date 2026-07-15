package com.example.attendee_university.model.dto.holiday.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolidayResponse {
    private UUID id;
    private LocalDate date;
    private String name;
    private UUID createdBy;
    private String createdByName;
    private Instant createdAt;
}