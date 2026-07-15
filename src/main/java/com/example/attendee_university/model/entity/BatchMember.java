package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "batch_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"batch_id", "app_user_id"}))
public class BatchMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "app_user_id", nullable = false)
    private UUID appUserId;

    @Column(columnDefinition = "TIMESTAMPTZ", name = "joined_at", updatable = false)
    @Builder.Default
    private Instant joinedAt = Instant.now();
}