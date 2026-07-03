package com.example.attendee_university.model.entity;

import com.example.attendee_university.model.constraint.ShiftType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A cohort/class within a Batch + Major, e.g. Batch 14 · Computer Science ·
 * "E3" (evening shift). Course scheduling now lives in TimetableSlot
 * (a group can meet for several different courses across the week) — this
 * entity still carries courseId/instructorId for backward compatibility with
 * the already-shipped frontend Groups page, but those two fields are legacy:
 * treat them as the group's nominal/default course, not the source of truth
 * for what it actually studies. New work should read TimetableSlot
 * instead. See CLAUDE.md for the planned frontend migration off these fields.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "batch_id")
    private UUID batchId;

    @Column(name = "major_id")
    private UUID majorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift")
    private ShiftType shift;

    /** @deprecated legacy single-course link — see TimetableSlot */
    @Deprecated
    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "name", nullable = false)
    private String name;

    /** @deprecated legacy single-instructor link — see TimetableSlot */
    @Deprecated
    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "semester")
    private String semester;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}