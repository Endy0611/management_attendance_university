package com.example.attendee_university.repository;

import com.example.attendee_university.model.constraint.AttendanceStatus;
import com.example.attendee_university.model.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, UUID> {

    boolean existsBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(UUID sessionId, UUID studentId);

    List<AttendanceRecord> findBySessionId(UUID sessionId);

    List<AttendanceRecord> findByStudentId(UUID studentId);

    long countBySessionIdAndStatus(UUID sessionId, AttendanceStatus status);
}