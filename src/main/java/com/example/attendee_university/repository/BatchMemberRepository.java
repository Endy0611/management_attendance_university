package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.BatchMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BatchMemberRepository extends JpaRepository<BatchMember, UUID> {

    List<BatchMember> findByBatchId(UUID batchId);

    List<BatchMember> findByAppUserId(UUID appUserId);

    Optional<BatchMember> findByBatchIdAndAppUserId(UUID batchId, UUID appUserId);

    long countByBatchId(UUID batchId);

    void deleteByBatchIdAndAppUserId(UUID batchId, UUID appUserId);
}