package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MajorRepository extends JpaRepository<Major, UUID> {
}