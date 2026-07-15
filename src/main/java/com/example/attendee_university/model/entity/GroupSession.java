package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_sessions")
public class GroupSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "zone_id", nullable = false)
    private UUID zoneId;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "start_time", nullable = false)
    private Instant startTime;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "end_time", nullable = false)
    private Instant endTime;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    // set when this session was auto-generated from a TimetableSlot template; null for one-off sessions
    @Column(name = "timetable_slot_id")
    private UUID timetableSlotId;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    // ── Single source of truth for session state ───────────────
    // Convention: [startTime, endTime) — inclusive of the start instant,
    // exclusive of the end instant. Every place in the codebase that needs
    // to know if a session is "active" must call this instead of rolling
    // its own isBefore/isAfter check.
    @Transient
    public boolean isActive(Instant now) {
        return !now.isBefore(startTime) && now.isBefore(endTime);
    }

    @Transient
    public boolean isUpcoming(Instant now) {
        return now.isBefore(startTime);
    }

    @Transient
    public boolean isExpired(Instant now) {
        return !now.isBefore(endTime);
    }
}