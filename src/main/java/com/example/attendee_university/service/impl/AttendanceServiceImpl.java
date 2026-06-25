package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.ForbiddenException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.AttendanceStatus;
import com.example.attendee_university.model.constraint.NotificationType;
import com.example.attendee_university.model.constraint.RoleType;
import com.example.attendee_university.model.dto.attendance.request.AttendanceCheckInRequest;
import com.example.attendee_university.model.dto.attendance.response.AbsentStudentResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceSummaryResponse;
import com.example.attendee_university.model.dto.attendance.response.StudentAttendanceResponse;
import com.example.attendee_university.model.dto.websocket.AttendanceEvent;
import com.example.attendee_university.model.dto.websocket.NotificationEvent;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.AttendanceRecord;
import com.example.attendee_university.model.entity.Group;
import com.example.attendee_university.model.entity.GroupMember;
import com.example.attendee_university.model.entity.GroupSession;
import com.example.attendee_university.model.entity.Zone;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.AttendanceRepository;
import com.example.attendee_university.repository.CourseRepository;
import com.example.attendee_university.repository.GroupMemberRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.repository.GroupSessionRepository;
import com.example.attendee_university.repository.ZoneRepository;
import com.example.attendee_university.service.AttendanceService;
import com.example.attendee_university.utils.GeoUtils;
import com.example.attendee_university.utils.HandleCurrentUser;
import com.example.attendee_university.websocket.handler.WebSocketPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository   attendanceRepository;
    private final GroupSessionRepository groupSessionRepository;
    private final GroupRepository        groupRepository;
    private final GroupMemberRepository  groupMemberRepository;
    private final ZoneRepository         zoneRepository;
    private final AppUserRepository      appUserRepository;
    private final CourseRepository       courseRepository;
    private final HandleCurrentUser      handleCurrentUser;
    private final GeoUtils               geoUtils;
    private final WebSocketPublisher     wsPublisher;

    // ── Check-in (STUDENT) ────────────────────────────────────
    @Override
    @Transactional
    public AttendanceResponse checkIn(UUID sessionId, AttendanceCheckInRequest request) {
        AppUser student = handleCurrentUser.getCurrentUser();

        if (student.getRole() != RoleType.STUDENT) {
            throw new ForbiddenException("Only students can check in.");
        }

        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(session.getStartTime()) || now.isAfter(session.getEndTime())) {
            throw new BadRequestException("This session is not currently active.");
        }

        if (!groupMemberRepository.existsByGroupIdAndAppUserId(session.getGroupId(), student.getId())) {
            throw new ForbiddenException("You are not enrolled in this group.");
        }

        if (attendanceRepository.existsBySessionIdAndStudentId(sessionId, student.getId())) {
            throw new BadRequestException("You have already checked in for this session.");
        }

        Zone zone = zoneRepository.findById(session.getZoneId())
                .orElseThrow(() -> new NotFoundException("Zone not found."));

        double distance = geoUtils.distanceMeters(
                request.latitude(), request.longitude(),
                zone.getLatitude(), zone.getLongitude()
        );

        if (distance > zone.getRadiusMeters()) {
            wsPublisher.notifyUser(student.getEmail(), NotificationEvent.builder()
                    .type(NotificationType.CHECK_IN_FAILED)
                    .title("Check-in failed")
                    .message(String.format("You are %.0f m away from the zone (allowed: %.0f m).",
                            distance, zone.getRadiusMeters()))
                    .timestamp(LocalDateTime.now())
                    .build());

            throw new BadRequestException(
                    String.format("You are %.0f m away from the zone (allowed: %.0f m).",
                            distance, zone.getRadiusMeters()));
        }

        AttendanceStatus status = now.isBefore(session.getStartTime().plusMinutes(15))
                ? AttendanceStatus.PRESENT
                : AttendanceStatus.LATE;

        AttendanceRecord record = AttendanceRecord.builder()
                .sessionId(sessionId)
                .studentId(student.getId())
                .checkedInAt(now)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .distanceMeters(distance)
                .status(status)
                .build();

        record = attendanceRepository.save(record);
        log.info("Student {} checked in for session {} — status: {}", student.getEmail(), sessionId, status);

        wsPublisher.notifyUser(student.getEmail(), NotificationEvent.builder()
                .type(NotificationType.CHECK_IN_SUCCESS)
                .title("Check-in recorded")
                .message("You are marked " + status.name() + " for this session.")
                .timestamp(LocalDateTime.now())
                .build());

        long totalPresent = attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.PRESENT);
        long totalLate    = attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.LATE);

        wsPublisher.publishAttendance(sessionId, AttendanceEvent.builder()
                .sessionId(sessionId)
                .studentId(student.getId())
                .studentName(student.getName())
                .studentNumber(student.getStudentId())
                .status(status)
                .distanceMeters(distance)
                .checkedInAt(record.getCheckedInAt())
                .totalPresent(totalPresent)
                .totalLate(totalLate)
                .build());

        return toResponse(record, student);
    }

    // ── Session attendance list ───────────────────────────────
    @Override
    public List<AttendanceResponse> getSessionAttendance(UUID sessionId) {
        groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(record -> {
                    AppUser student = appUserRepository.findById(record.getStudentId()).orElse(null);
                    return toResponse(record, student);
                })
                .toList();
    }

    // ── Absent students list ──────────────────────────────────
    @Override
    public List<AbsentStudentResponse> getAbsentStudents(UUID sessionId) {
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        Set<UUID> checkedInIds = attendanceRepository.findBySessionId(sessionId).stream()
                .map(AttendanceRecord::getStudentId)
                .collect(Collectors.toSet());

        return groupMemberRepository.findByGroupId(session.getGroupId()).stream()
                .filter(member -> !checkedInIds.contains(member.getAppUserId()))
                .map(member -> {
                    AppUser student = appUserRepository.findById(member.getAppUserId()).orElse(null);
                    return AbsentStudentResponse.builder()
                            .studentId(member.getAppUserId())
                            .studentName(student != null ? student.getName() : null)
                            .studentEmail(student != null ? student.getEmail() : null)
                            .studentNumber(student != null ? student.getStudentId() : null)
                            .build();
                })
                .toList();
    }

    // ── Manual override (Admin / Instructor) ──────────────────
    @Override
    @Transactional
    public AttendanceResponse manualOverride(UUID sessionId, UUID studentId, String status) {
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        if (!groupMemberRepository.existsByGroupIdAndAppUserId(session.getGroupId(), studentId)) {
            throw new BadRequestException("Student is not enrolled in this group.");
        }

        AttendanceStatus newStatus;
        try {
            newStatus = AttendanceStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Use: PRESENT, LATE, or ABSENT.");
        }

        AppUser student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));

        AttendanceRecord record = attendanceRepository
                .findBySessionIdAndStudentId(sessionId, studentId)
                .orElseGet(() -> AttendanceRecord.builder()
                        .sessionId(sessionId)
                        .studentId(studentId)
                        .checkedInAt(LocalDateTime.now())
                        .latitude(0.0)
                        .longitude(0.0)
                        .distanceMeters(0.0)
                        .build());

        record.setStatus(newStatus);
        record = attendanceRepository.save(record);

        log.info("Manual override: session={} student={} status={}", sessionId, studentId, newStatus);
        return toResponse(record, student);
    }

    // ── My attendance history (STUDENT) ───────────────────────
    @Override
    public List<StudentAttendanceResponse> getMyAttendance() {
        AppUser student = handleCurrentUser.getCurrentUser();
        return buildStudentHistory(student.getId());
    }

    // ── All past sessions for my enrolled groups (STUDENT) ────
    @Override
    public List<StudentAttendanceResponse> getSessionHistoryForMyGroups() {
        AppUser student = handleCurrentUser.getCurrentUser();

        List<UUID> groupIds = groupMemberRepository.findByAppUserId(student.getId()).stream()
                .map(GroupMember::getGroupId)
                .toList();

        LocalDateTime now = LocalDateTime.now();

        return groupIds.stream()
                .flatMap(groupId -> groupSessionRepository.findByGroupId(groupId).stream())
                .filter(session -> session.getEndTime().isBefore(now))
                .map(session -> {
                    Group group = groupRepository.findById(session.getGroupId()).orElse(null);
                    String courseCode = group != null
                            ? courseRepository.findById(group.getCourseId())
                            .map(c -> c.getCode()).orElse(null)
                            : null;

                    AttendanceStatus status = attendanceRepository
                            .findBySessionIdAndStudentId(session.getId(), student.getId())
                            .map(AttendanceRecord::getStatus)
                            .orElse(AttendanceStatus.ABSENT);

                    LocalDateTime checkedInAt = attendanceRepository
                            .findBySessionIdAndStudentId(session.getId(), student.getId())
                            .map(AttendanceRecord::getCheckedInAt)
                            .orElse(null);

                    return StudentAttendanceResponse.builder()
                            .attendanceId(null)
                            .sessionId(session.getId())
                            .sessionTitle(group != null
                                    ? group.getName() + " — " + session.getStartTime().toLocalDate()
                                    : "Unknown session")
                            .groupName(group != null ? group.getName() : null)
                            .courseCode(courseCode)
                            .status(status)
                            .checkedInAt(checkedInAt)
                            .build();
                })
                .toList();
    }

    // ── Session summary ───────────────────────────────────────
    @Override
    public AttendanceSummaryResponse getSessionSummary(UUID sessionId) {
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        long totalStudents = groupMemberRepository.countByGroupId(session.getGroupId());
        long present = attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.PRESENT);
        long late    = attendanceRepository.countBySessionIdAndStatus(sessionId, AttendanceStatus.LATE);
        long absent  = Math.max(0, totalStudents - present - late);

        return AttendanceSummaryResponse.builder()
                .sessionId(sessionId)
                .totalStudents(totalStudents)
                .present(present)
                .late(late)
                .absent(absent)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────
    private List<StudentAttendanceResponse> buildStudentHistory(UUID studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(record -> {
                    GroupSession session = groupSessionRepository.findById(record.getSessionId()).orElse(null);
                    Group group = session != null
                            ? groupRepository.findById(session.getGroupId()).orElse(null) : null;
                    String courseCode = group != null
                            ? courseRepository.findById(group.getCourseId())
                            .map(c -> c.getCode()).orElse(null)
                            : null;

                    return StudentAttendanceResponse.builder()
                            .attendanceId(record.getId())
                            .sessionId(record.getSessionId())
                            .sessionTitle(session != null && group != null
                                    ? group.getName() + " — " + session.getStartTime().toLocalDate()
                                    : "Unknown session")
                            .groupName(group != null ? group.getName() : null)
                            .courseCode(courseCode)
                            .status(record.getStatus())
                            .checkedInAt(record.getCheckedInAt())
                            .build();
                })
                .toList();
    }

    private AttendanceResponse toResponse(AttendanceRecord record, AppUser student) {
        return AttendanceResponse.builder()
                .attendanceId(record.getId())
                .sessionId(record.getSessionId())
                .studentId(record.getStudentId())
                .studentName(student != null ? student.getName() : null)
                .checkedInAt(record.getCheckedInAt())
                .latitude(record.getLatitude())
                .longitude(record.getLongitude())
                .distanceMeters(record.getDistanceMeters())
                .status(record.getStatus())
                .build();
    }
}