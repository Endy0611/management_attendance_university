package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.FaceEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FaceEmbeddingRepository extends JpaRepository<FaceEmbedding, UUID> {

    Optional<FaceEmbedding> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}