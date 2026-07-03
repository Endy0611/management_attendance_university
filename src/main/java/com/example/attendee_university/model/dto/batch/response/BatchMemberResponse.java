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
public class BatchMemberResponse {
    private UUID          studentId;
    private String        studentName;
    private String        studentEmail;
    private String        studentNumber;
    private LocalDateTime joinedAt;
}