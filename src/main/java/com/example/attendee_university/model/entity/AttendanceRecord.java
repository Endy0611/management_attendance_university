package com.example.attendee_university.model.entity;

import com.example.attendee_university.model.constraint.AttendanceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "session_id",
                                "student_id"
                        }
                )
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "distance_meters", nullable = false)
    private Double distanceMeters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}