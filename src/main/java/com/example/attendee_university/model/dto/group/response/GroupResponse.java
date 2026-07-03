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

    // legacy default course/instructor — see TimetableSlot for the
    // actual multi-course weekly schedule
    private UUID courseId;
    private String courseCode;
    private UUID instructorId;
    private String instructorName;

    private String name;
    private int capacity;
    private int memberCount;
    private String semester;

    private UUID batchId;
    private String batchName;
    private UUID majorId;
    private String majorName;
    private String shift;

    private LocalDateTime createdAt;
}