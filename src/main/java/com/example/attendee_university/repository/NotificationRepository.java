package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.NotificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationRecord, UUID> {

    List<NotificationRecord> findByUserIdOrderByCreatedAtDesc(UUID userId);

    long countByUserIdAndReadFalse(UUID userId);

    @Modifying
    @Query("UPDATE NotificationRecord n SET n.read = true WHERE n.userId = :userId")
    void markAllReadByUserId(@Param("userId") UUID userId);
}