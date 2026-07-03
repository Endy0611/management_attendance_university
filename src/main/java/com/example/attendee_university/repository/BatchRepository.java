package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BatchRepository extends JpaRepository<Batch, UUID> {
}