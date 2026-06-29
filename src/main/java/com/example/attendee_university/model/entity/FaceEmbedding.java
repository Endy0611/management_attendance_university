package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "face_embeddings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    // 128-dim vector stored as comma-separated floats from FastAPI
    @Column(name = "embedding_vector", nullable = false, columnDefinition = "TEXT")
    private String embeddingVector;

    @Column(name = "registered_at", updatable = false)
    @Builder.Default
    private LocalDateTime registeredAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}