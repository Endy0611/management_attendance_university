package com.example.attendee_university.model.dto.group.response;

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
public class GroupResponse {
    private UUID id;
    private UUID courseId;
    private String courseCode;
    private String name;
    private UUID instructorId;
    private String instructorName;
    private int capacity;
    private int memberCount;
    private String semester;
    private LocalDateTime createdAt;
}