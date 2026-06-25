package com.example.attendee_university.model.dto.websocket;

import com.example.attendee_university.model.constraint.AttendanceStatus;
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
public class AttendanceEvent {

    private UUID sessionId;
    private UUID studentId;
    private String studentName;
    private String studentNumber;   // studentId field on AppUser
    private AttendanceStatus status;
    private Double distanceMeters;
    private LocalDateTime checkedInAt;
    private long totalPresent;      // running count after this check-in
    private long totalLate;
}