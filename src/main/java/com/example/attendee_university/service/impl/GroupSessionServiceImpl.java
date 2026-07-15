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
        return toResponse(saved, group, zone);
    }

    @Override
    public List<GroupSessionResponse> getAllSessions() {
        return groupSessionRepository.findAll().stream()
                .map(this::toResponseResolved)
                .toList();
    }

    @Override
    public List<GroupSessionResponse> getSessionsByGroup(UUID groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found."));
        return groupSessionRepository.findByGroupId(groupId).stream()
                .map(this::toResponseResolved)
                .toList();
    }

    @Override
    public GroupSessionResponse getSessionById(UUID id) {
        return toResponseResolved(findOrThrow(id));
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

        return toResponse(session, group, zone);
    }

    @Override
    @Transactional
    public void deleteSession(UUID id) {
        GroupSession session = findOrThrow(id);
        groupSessionRepository.delete(session);
        log.info("Session {} deleted", id);
    }

    @Override
    public List<GroupSessionResponse> getMyActiveSessions() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        Instant now = Instant.now();

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

        return groupSessionRepository.findActiveByGroupIds(groupIds, now).stream()
                .map(this::toResponseResolved)
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
                .map(this::toResponseResolved)
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
                .map(this::toResponseResolved)
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

    private GroupSessionResponse toResponseResolved(GroupSession session) {
        Group group = groupRepository.findById(session.getGroupId()).orElse(null);
        Zone zone = zoneRepository.findById(session.getZoneId()).orElse(null);
        return toResponse(session, group, zone);
    }

    private GroupSessionResponse toResponse(GroupSession session, Group group, Zone zone) {
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
                .createdAt(session.getCreatedAt())
                .build();
    }
}