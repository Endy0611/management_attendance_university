package com.example.attendee_university.model.dto.group.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponse {
    private UUID          studentId;
    private String        studentName;
    private String        studentEmail;
    private String        studentNumber;
    private Instant joinedAt;
}