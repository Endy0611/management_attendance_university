package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.GroupSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GroupSessionRepository extends JpaRepository<GroupSession, UUID> {

    List<GroupSession> findByGroupId(UUID groupId);

    List<GroupSession> findByZoneId(UUID zoneId);

    @Query("SELECT s FROM GroupSession s WHERE s.groupId IN :groupIds " +
            "AND s.startTime <= :now AND s.endTime >= :now")
    List<GroupSession> findActiveByGroupIds(@Param("groupIds") List<UUID> groupIds, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM GroupSession s WHERE s.groupId = :groupId " +
            "AND s.startTime <= :now AND s.endTime >= :now")
    List<GroupSession> findActiveByGroupId(@Param("groupId") UUID groupId, @Param("now") LocalDateTime now);
}