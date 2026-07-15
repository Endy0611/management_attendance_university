package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A university-wide blackout date (public holiday, semester break, etc).
 * {@link com.example.attendee_university.service.impl.TimetableSlotServiceImpl#generateSessionsForSlot}
 * skips generating a {@link GroupSession} for any date that has a matching Holiday row,
 * and extends generation to the next non-holiday occurrence instead — so
 * TimetableSlot.totalSessions always means "this many real class sessions",
 * not "this many calendar weeks".
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "holidays", uniqueConstraints = @UniqueConstraint(columnNames = "date"))
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}