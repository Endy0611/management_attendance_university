package com.example.attendee_university.model.dto.batch.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    private UUID id;
    private String name;
    private int intakeYear;
    private int groupCount;
    private LocalDateTime createdAt;
}