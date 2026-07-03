package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.GroupSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    // ── Upcoming (not-yet-started) sessions ─────────────────────
    @Query("SELECT s FROM GroupSession s WHERE s.groupId IN :groupIds " +
            "AND s.startTime > :now ORDER BY s.startTime ASC")
    List<GroupSession> findUpcomingByGroupIds(@Param("groupIds") List<UUID> groupIds, @Param("now") LocalDateTime now);

    // ── Conflict checks for one-off / generated sessions ───────
    @Query("SELECT s FROM GroupSession s WHERE s.zoneId = :zoneId AND s.id <> :excludeId " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<GroupSession> findOverlappingByZone(@Param("zoneId") UUID zoneId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("excludeId") UUID excludeId);

    @Query("SELECT s FROM GroupSession s WHERE s.groupId IN :groupIds AND s.id <> :excludeId " +
            "AND s.startTime < :endTime AND s.endTime > :startTime")
    List<GroupSession> findOverlappingByGroupIds(@Param("groupIds") List<UUID> groupIds,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 @Param("excludeId") UUID excludeId);

    // ── Generated-session bookkeeping for TimetableSlot ─────────
    boolean existsByTimetableSlotIdAndStartTime(UUID timetableSlotId, LocalDateTime startTime);

    List<GroupSession> findByTimetableSlotId(UUID timetableSlotId);

    // used instead of findByTimetableSlotId(...).size() to avoid loading entities just to count them
    long countByTimetableSlotId(UUID timetableSlotId);

    @Modifying
    @Query("DELETE FROM GroupSession s WHERE s.timetableSlotId = :slotId AND s.startTime > :now")
    void deleteFutureByTimetableSlotId(@Param("slotId") UUID slotId, @Param("now") LocalDateTime now);
}