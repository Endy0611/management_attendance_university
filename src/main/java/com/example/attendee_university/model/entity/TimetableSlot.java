package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A recurring weekly slot for a group/batch, e.g.
 * "E3 (CS) meets every Monday 17:30-19:00 in G-104, for 10 sessions starting 16-Mar-26".
 * {@link GroupSessionRepository} generates the actual dated {@link GroupSession} rows from this template.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "timetable_slots")
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "zone_id", nullable = false)
    private UUID zoneId;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // first calendar date this weekly slot is eligible; if validFrom does not fall on dayOfWeek,
    // the first generated session lands on the next occurrence of dayOfWeek on/after validFrom
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    // how many weekly occurrences this slot generates (e.g. 10 sessions in a semester)
    @Column(name = "total_sessions", nullable = false)
    private int totalSessions;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
