package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_sessions")
public class GroupSession {

    // How early before startTime a student is allowed to check in.
    public static final Duration EARLY_CHECKIN_WINDOW = Duration.ofMinutes(30);

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
    // Convention: check-in window is [startTime - EARLY_CHECKIN_WINDOW, endTime)
    // — opens 30 min before the official start, exclusive of the end instant.
    // Every place in the codebase that needs to know if a session is
    // "active" (i.e. checkinable) must call this instead of rolling its
    // own isBefore/isAfter check.
    @Transient
    public boolean isActive(Instant now) {
        Instant checkinOpensAt = startTime.minus(EARLY_CHECKIN_WINDOW);
        return !now.isBefore(checkinOpensAt) && now.isBefore(endTime);
    }

    // Kept in sync with isActive's checkinOpensAt so "upcoming" and "active"
    // never overlap or leave a gap — upcoming now means "before the early
    // check-in window opens", not "before startTime".
    @Transient
    public boolean isUpcoming(Instant now) {
        Instant checkinOpensAt = startTime.minus(EARLY_CHECKIN_WINDOW);
        return now.isBefore(checkinOpensAt);
    }

    @Transient
    public boolean isExpired(Instant now) {
        return !now.isBefore(endTime);
    }
}