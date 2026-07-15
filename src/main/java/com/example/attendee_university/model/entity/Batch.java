package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * An intake cohort, e.g. "Batch 14". Students enroll into a batch once per
 * program; a batch spans multiple majors and, within each major, multiple
 * groups (see Group).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "intake_year", nullable = false)
    private int intakeYear;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "created_at", updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}