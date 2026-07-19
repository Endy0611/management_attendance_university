package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.RoleType;
import com.example.attendee_university.model.dto.session.request.GroupSessionRequest;
import com.example.attendee_university.model.dto.session.response.GroupSessionResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.Group;
import com.example.attendee_university.model.entity.GroupMember;
import com.example.attendee_university.model.entity.GroupSession;
import com.example.attendee_university.model.entity.Zone;
import com.example.attendee_university.repository.AttendanceRepository;
import com.example.attendee_university.repository.GroupMemberRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.repository.GroupSessionRepository;
import com.example.attendee_university.repository.CourseRepository;
import com.example.attendee_university.repository.ZoneRepository;
import com.example.attendee_university.model.entity.Course;
import com.example.attendee_university.service.GroupSessionService;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupSessionServiceImpl implements GroupSessionService {

    private final GroupSessionRepository groupSessionRepository;
    private final GroupRepository groupRepository;
    private final ZoneRepository zoneRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CourseRepository        courseRepository;
    private final AttendanceRepository    attendanceRepository;
    private final HandleCurrentUser handleCurrentUser;

    // sentinel "exclude nothing" id for the overlap queries when creating a brand-new session
    private static final UUID NO_EXCLUDE = new UUID(0L, 0L);

    @Override
    @Transactional
    public GroupSessionResponse createSession(GroupSessionRequest request) {
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new NotFoundException("Zone not found."));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }

        assertNoTimeConflict(group, zone, request.startTime(), request.endTime(), null);

        UUID currentUserId = handleCurrentUser.getUserIdOfCurrentUser();

        GroupSession session = GroupSession.builder()
                .groupId(group.getId())
                .zoneId(zone.getId())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .createdBy(currentUserId)
                .build();

        GroupSession saved = groupSessionRepository.save(session);
        log.info("Session created for group {} in zone {}", group.getName(), zone.getName());
        return toResponse(saved, group, zone, false);
    }

    @Override
    public List<GroupSessionResponse> getAllSessions() {
        return groupSessionRepository.findAll().stream()
                .map(s -> toResponseResolved(s, false))
                .toList();
    }

    @Override
    public List<GroupSessionResponse> getSessionsByGroup(UUID groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found."));
        return groupSessionRepository.findByGroupId(groupId).stream()
                .map(s -> toResponseResolved(s, false))
                .toList();
    }

    @Override
    public GroupSessionResponse getSessionById(UUID id) {
        return toResponseResolved(findOrThrow(id), false);
    }

    @Override
    @Transactional
    public GroupSessionResponse updateSession(UUID id, GroupSessionRequest request) {
        GroupSession session = findOrThrow(id);

        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new NotFoundException("Zone not found."));

        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("End time must be after start time.");
        }

        assertNoTimeConflict(group, zone, request.startTime(), request.endTime(), id);

        session.setGroupId(group.getId());
        session.setZoneId(zone.getId());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        // dirty checking saves automatically inside @Transactional

        return toResponse(session, group, zone, false);
    }

    @Override
    @Transactional
    public void deleteSession(UUID id) {
        GroupSession session = findOrThrow(id);
        groupSessionRepository.delete(session);
        log.info("Session {} deleted", id);
    }

    // ── Active (checkinable) sessions for current user ──────────
    // For STUDENT callers this is the list that drives the check-in page,
    // so each entry is annotated with alreadyCheckedIn — the frontend uses
    // that to hide the check-in flow entirely for a session the student
    // has already submitted, instead of only finding out after tapping
    // Check In and getting AttendanceServiceImpl's "already checked in"
    // rejection.
    @Override
    public List<GroupSessionResponse> getMyActiveSessions() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        Instant now = Instant.now();
        Instant checkinCutoff = now.plus(GroupSession.EARLY_CHECKIN_WINDOW);

        List<UUID> groupIds;
        if (currentUser.getRole() == RoleType.ADMIN) {
            groupIds = groupRepository.findAll().stream()
                    .map(Group::getId)
                    .toList();
        } else if (currentUser.getRole() == RoleType.INSTRUCTOR) {
            groupIds = groupRepository.findByInstructorId(currentUser.getId()).stream()
                    .map(Group::getId)
                    .toList();
        } else {
            // STUDENT — only groups they're enrolled in
            groupIds = groupMemberRepository.findByAppUserId(currentUser.getId()).stream()
                    .map(GroupMember::getGroupId)
                    .toList();
        }

        if (groupIds.isEmpty()) return List.of();

        boolean isStudent = currentUser.getRole() == RoleType.STUDENT;

        return groupSessionRepository.findActiveByGroupIds(groupIds, checkinCutoff, now).stream()
                .map(session -> {
                    boolean alreadyCheckedIn = isStudent
                            && attendanceRepository.existsBySessionIdAndStudentId(session.getId(), currentUser.getId());
                    return toResponseResolved(session, alreadyCheckedIn);
                })
                .toList();
    }

    // ── Past sessions for current student's enrolled groups ────
    @Override
    public List<GroupSessionResponse> getPastSessionsForMyGroups() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        Instant now = Instant.now();

        List<UUID> groupIds = groupMemberRepository.findByAppUserId(currentUser.getId()).stream()
                .map(GroupMember::getGroupId)
                .toList();

        if (groupIds.isEmpty()) return List.of();

        return groupIds.stream()
                .flatMap(groupId -> groupSessionRepository.findByGroupId(groupId).stream())
                .filter(session -> session.isExpired(now))
                .map(session -> {
                    boolean alreadyCheckedIn = attendanceRepository
                            .existsBySessionIdAndStudentId(session.getId(), currentUser.getId());
                    return toResponseResolved(session, alreadyCheckedIn);
                })
                .toList();
    }

    // ── Upcoming (not-yet-started) sessions for current user's groups ──
    @Override
    public List<GroupSessionResponse> getUpcomingSessionsForMyGroups() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        Instant now = Instant.now();

        List<UUID> groupIds;
        if (currentUser.getRole() == RoleType.ADMIN) {
            groupIds = groupRepository.findAll().stream().map(Group::getId).toList();
        } else if (currentUser.getRole() == RoleType.INSTRUCTOR) {
            groupIds = groupRepository.findByInstructorId(currentUser.getId()).stream()
                    .map(Group::getId).toList();
        } else {
            groupIds = groupMemberRepository.findByAppUserId(currentUser.getId()).stream()
                    .map(GroupMember::getGroupId).toList();
        }

        if (groupIds.isEmpty()) return List.of();

        return groupSessionRepository.findUpcomingByGroupIds(groupIds, now).stream()
                .map(s -> toResponseResolved(s, false))
                .toList();
    }

    // ── Helpers ────────────────────────────────────────────────
    // real-world conflict rules: no room double-booking, no instructor double-booking at the same time
    private void assertNoTimeConflict(Group group, Zone zone, Instant startTime, Instant endTime, UUID excludeSessionId) {
        UUID excludeId = excludeSessionId != null ? excludeSessionId : NO_EXCLUDE;

        List<GroupSession> zoneConflicts = groupSessionRepository.findOverlappingByZone(zone.getId(), startTime, endTime, excludeId);
        if (!zoneConflicts.isEmpty()) {
            throw new BadRequestException("Room \"" + zone.getName() + "\" is already booked for an overlapping time.");
        }

        List<UUID> instructorGroupIds = groupRepository.findByInstructorId(group.getInstructorId()).stream()
                .map(Group::getId).toList();
        List<GroupSession> instructorConflicts = groupSessionRepository.findOverlappingByGroupIds(instructorGroupIds, startTime, endTime, excludeId);
        if (!instructorConflicts.isEmpty()) {
            throw new BadRequestException("Instructor already has a session at an overlapping time.");
        }
    }

    private GroupSession findOrThrow(UUID id) {
        return groupSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Session not found."));
    }

    private GroupSessionResponse toResponseResolved(GroupSession session, boolean alreadyCheckedIn) {
        Group group = groupRepository.findById(session.getGroupId()).orElse(null);
        Zone zone = zoneRepository.findById(session.getZoneId()).orElse(null);
        return toResponse(session, group, zone, alreadyCheckedIn);
    }

    private GroupSessionResponse toResponse(GroupSession session, Group group, Zone zone, boolean alreadyCheckedIn) {
        boolean active = session.isActive(Instant.now());

        // resolve courseCode via CourseRepository
        String courseCode = null;
        if (group != null) {
            courseCode = courseRepository.findById(group.getCourseId())
                    .map(Course::getCode)
                    .orElse(null);
        }

        return GroupSessionResponse.builder()
                .id(session.getId())
                .groupId(session.getGroupId())
                .groupName(group != null ? group.getName() : null)
                .courseCode(courseCode)
                .zoneId(session.getZoneId())
                .zoneName(zone != null ? zone.getName() : null)
                .latitude(zone != null ? zone.getLatitude() : 0)
                .longitude(zone != null ? zone.getLongitude() : 0)
                .radiusMeters(zone != null ? zone.getRadiusMeters() : 0)
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .active(active)
                .alreadyCheckedIn(alreadyCheckedIn)
                .createdAt(session.getCreatedAt())
                .build();
    }
}