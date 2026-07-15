package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.ForbiddenException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.RoleType;
import com.example.attendee_university.model.dto.timetable.request.TimetableSlotRequest;
import com.example.attendee_university.model.dto.timetable.response.TimetableSlotResponse;
import com.example.attendee_university.model.entity.*;
import com.example.attendee_university.repository.*;
import com.example.attendee_university.service.TimetableSlotService;
import com.example.attendee_university.utils.AppTimeZone;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableSlotServiceImpl implements TimetableSlotService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final GroupSessionRepository groupSessionRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ZoneRepository zoneRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final HandleCurrentUser handleCurrentUser;

    // ── Create slot (Admin, or the owning Instructor) ───────────
    @Override
    @Transactional
    public TimetableSlotResponse createSlot(TimetableSlotRequest request) {
        Group group = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        assertCanManageGroup(group);

        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new NotFoundException("Zone not found."));

        validateTimeRange(request.startTime(), request.endTime());
        assertNoConflicts(request, group, null);

        UUID currentUserId = handleCurrentUser.getUserIdOfCurrentUser();

        TimetableSlot slot = TimetableSlot.builder()
                .groupId(group.getId())
                .zoneId(zone.getId())
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .validFrom(request.validFrom())
                .totalSessions(request.totalSessions())
                .createdBy(currentUserId)
                .build();

        TimetableSlot saved = timetableSlotRepository.save(slot);
        int generated = generateSessionsForSlot(saved);
        log.info("Timetable slot created for group {} on {} {}-{} ({} sessions generated)",
                group.getName(), saved.getDayOfWeek(), saved.getStartTime(), saved.getEndTime(), generated);

        return toResponse(saved, group, zone);
    }

    // ── Update slot (Admin, or the owning Instructor) ───────────
    @Override
    @Transactional
    public TimetableSlotResponse updateSlot(UUID id, TimetableSlotRequest request) {
        TimetableSlot slot = findOrThrow(id);
        Group currentGroup = groupRepository.findById(slot.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        assertCanManageGroup(currentGroup);

        Group newGroup = groupRepository.findById(request.groupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        assertCanManageGroup(newGroup);

        Zone zone = zoneRepository.findById(request.zoneId())
                .orElseThrow(() -> new NotFoundException("Zone not found."));

        validateTimeRange(request.startTime(), request.endTime());
        assertNoConflicts(request, newGroup, id);

        // only wipe/regenerate future sessions if the actual schedule changed - avoids discarding
        // future GroupSessions (e.g. reminders, RSVPs) on a no-op update or a totalSessions-only bump
        boolean scheduleChanged = !slot.getGroupId().equals(newGroup.getId())
                || !slot.getZoneId().equals(zone.getId())
                || slot.getDayOfWeek() != request.dayOfWeek()
                || !slot.getStartTime().equals(request.startTime())
                || !slot.getEndTime().equals(request.endTime())
                || !slot.getValidFrom().equals(request.validFrom());

        if (scheduleChanged) {
            // drop future auto-generated sessions for the old schedule; past ones (with attendance history) are untouched
            groupSessionRepository.deleteFutureByTimetableSlotId(id, Instant.now());
        }

        slot.setGroupId(newGroup.getId());
        slot.setZoneId(zone.getId());
        slot.setDayOfWeek(request.dayOfWeek());
        slot.setStartTime(request.startTime());
        slot.setEndTime(request.endTime());
        slot.setValidFrom(request.validFrom());
        slot.setTotalSessions(request.totalSessions());
        // dirty checking saves automatically inside @Transactional

        // idempotent regardless of scheduleChanged: skips occurrences that already exist,
        // and appends new future weeks if totalSessions grew
        int generated = generateSessionsForSlot(slot);
        log.info("Timetable slot {} updated ({} sessions (re)generated)", id, generated);

        return toResponse(slot, newGroup, zone);
    }

    // ── Delete slot (Admin, or the owning Instructor) ───────────
    @Override
    @Transactional
    public void deleteSlot(UUID id) {
        TimetableSlot slot = findOrThrow(id);
        Group group = groupRepository.findById(slot.getGroupId()).orElse(null);
        if (group != null) assertCanManageGroup(group);

        // only drop future occurrences; past sessions keep their attendance history
        groupSessionRepository.deleteFutureByTimetableSlotId(id, Instant.now());
        timetableSlotRepository.delete(slot);
        log.info("Timetable slot {} deleted", id);
    }

    // ── Get one slot (Admin, or the owning Instructor only) ─────
    @Override
    public TimetableSlotResponse getSlotById(UUID id) {
        TimetableSlot slot = findOrThrow(id);
        Group group = groupRepository.findById(slot.getGroupId())
                .orElseThrow(() -> new NotFoundException("Group not found."));
        assertCanManageGroup(group);

        Zone zone = zoneRepository.findById(slot.getZoneId()).orElse(null);
        return toResponse(slot, group, zone);
    }

    // ── Weekly timetable for one batch/group ────────────────────
    @Override
    public List<TimetableSlotResponse> getWeeklyTimetableForGroup(UUID groupId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found."));

        return timetableSlotRepository.findByGroupIdOrderByDayOfWeekAscStartTimeAsc(groupId).stream()
                .map(this::toResponseResolved)
                .sorted(Comparator.comparing(TimetableSlotResponse::getDayOfWeek)
                        .thenComparing(TimetableSlotResponse::getStartTime))
                .toList();
    }

    // ── My weekly timetable (role-aware) ────────────────────────
    @Override
    public List<TimetableSlotResponse> getMyWeeklyTimetable() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();

        List<UUID> groupIds = switch (currentUser.getRole()) {
            case ADMIN -> groupRepository.findAll().stream().map(Group::getId).toList();
            case INSTRUCTOR -> groupRepository.findByInstructorId(currentUser.getId()).stream()
                    .map(Group::getId).toList();
            case STUDENT -> groupMemberRepository.findByAppUserId(currentUser.getId()).stream()
                    .map(GroupMember::getGroupId).toList();
        };

        if (groupIds.isEmpty()) return List.of();

        // single batched query instead of one findByGroupId call per group
        return timetableSlotRepository.findByGroupIdIn(groupIds).stream()
                .map(this::toResponseResolved)
                .sorted(Comparator.comparing(TimetableSlotResponse::getDayOfWeek)
                        .thenComparing(TimetableSlotResponse::getStartTime))
                .toList();
    }

    // ── (Re)generate sessions on demand ─────────────────────────
    @Override
    @Transactional
    public int generateSessions(UUID slotId) {
        TimetableSlot slot = findOrThrow(slotId);
        Group group = groupRepository.findById(slot.getGroupId()).orElse(null);
        if (group != null) assertCanManageGroup(group);
        return generateSessionsForSlot(slot);
    }

    // ── Helpers ──────────────────────────────────────────────────
    private TimetableSlot findOrThrow(UUID id) {
        return timetableSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Timetable slot not found."));
    }

    private void assertCanManageGroup(Group group) {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        if (currentUser.getRole() == RoleType.ADMIN) return;

        if (currentUser.getRole() == RoleType.INSTRUCTOR
                && group.getInstructorId().equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException("You do not have permission to manage this group's timetable.");
    }

    private void validateTimeRange(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time.");
        }
    }

    // real-world conflict rules: no room double-booking, no instructor double-booking,
    // and no batch attending two classes at once — same as a university registrar would enforce.
    // The zone/room check uses a pessimistic write lock (see TimetableSlotRepository) so two
    // concurrent requests for the same room+day can't both pass the check before either commits.
    private void assertNoConflicts(TimetableSlotRequest request, Group group, UUID excludeSlotId) {
        DayOfWeek day = request.dayOfWeek();
        LocalTime start = request.startTime();
        LocalTime end = request.endTime();

        for (TimetableSlot s : timetableSlotRepository.findByZoneIdAndDayOfWeekForUpdate(request.zoneId(), day)) {
            if (sameSlot(s, excludeSlotId)) continue;
            if (overlaps(start, end, s.getStartTime(), s.getEndTime())) {
                throw new BadRequestException("Room is already booked on " + day + " "
                        + s.getStartTime() + "-" + s.getEndTime() + ".");
            }
        }

        List<UUID> instructorGroupIds = groupRepository.findByInstructorId(group.getInstructorId()).stream()
                .map(Group::getId).toList();
        for (TimetableSlot s : timetableSlotRepository.findByGroupIdInAndDayOfWeek(instructorGroupIds, day)) {
            if (sameSlot(s, excludeSlotId)) continue;
            if (overlaps(start, end, s.getStartTime(), s.getEndTime())) {
                throw new BadRequestException("Instructor already has a class on " + day + " "
                        + s.getStartTime() + "-" + s.getEndTime() + ".");
            }
        }

        for (TimetableSlot s : timetableSlotRepository.findByGroupIdAndDayOfWeek(group.getId(), day)) {
            if (sameSlot(s, excludeSlotId)) continue;
            if (overlaps(start, end, s.getStartTime(), s.getEndTime())) {
                throw new BadRequestException("This batch already has a class on " + day + " "
                        + s.getStartTime() + "-" + s.getEndTime() + ".");
            }
        }
    }

    private boolean sameSlot(TimetableSlot s, UUID excludeSlotId) {
        return excludeSlotId != null && s.getId().equals(excludeSlotId);
    }

    private boolean overlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // walks forward week by week from validFrom, creating the dated GroupSession occurrences.
    // idempotent: skips any occurrence that's already been generated for this slot.
    private int generateSessionsForSlot(TimetableSlot slot) {
        List<GroupSession> created = new ArrayList<>();

        LocalDate date = slot.getValidFrom();
        while (date.getDayOfWeek() != slot.getDayOfWeek()) {
            date = date.plusDays(1);
        }

        for (int i = 0; i < slot.getTotalSessions(); i++) {
            Instant start = date.atTime(slot.getStartTime()).atZone(AppTimeZone.CAMBODIA).toInstant();
            Instant end = date.atTime(slot.getEndTime()).atZone(AppTimeZone.CAMBODIA).toInstant();

            if (!groupSessionRepository.existsByTimetableSlotIdAndStartTime(slot.getId(), start)) {
                GroupSession session = GroupSession.builder()
                        .groupId(slot.getGroupId())
                        .zoneId(slot.getZoneId())
                        .startTime(start)
                        .endTime(end)
                        .createdBy(slot.getCreatedBy())
                        .timetableSlotId(slot.getId())
                        .build();
                created.add(groupSessionRepository.save(session));
            }
            date = date.plusWeeks(1);
        }

        return created.size();
    }

    private TimetableSlotResponse toResponseResolved(TimetableSlot slot) {
        Group group = groupRepository.findById(slot.getGroupId()).orElse(null);
        Zone zone = zoneRepository.findById(slot.getZoneId()).orElse(null);
        return toResponse(slot, group, zone);
    }

    private TimetableSlotResponse toResponse(TimetableSlot slot, Group group, Zone zone) {
        String courseCode = null;
        String instructorName = null;
        if (group != null) {
            courseCode = courseRepository.findById(group.getCourseId()).map(Course::getCode).orElse(null);
            instructorName = appUserRepository.findById(group.getInstructorId()).map(AppUser::getName).orElse(null);
        }
        // count query instead of loading + sizing the full session list
        long generatedCount = groupSessionRepository.countByTimetableSlotId(slot.getId());

        return TimetableSlotResponse.builder()
                .id(slot.getId())
                .groupId(slot.getGroupId())
                .groupName(group != null ? group.getName() : null)
                .courseCode(courseCode)
                .instructorName(instructorName)
                .zoneId(slot.getZoneId())
                .zoneName(zone != null ? zone.getName() : null)
                .dayOfWeek(slot.getDayOfWeek())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .validFrom(slot.getValidFrom())
                .totalSessions(slot.getTotalSessions())
                .generatedSessionsCount((int) generatedCount)
                .createdAt(slot.getCreatedAt())
                .build();
    }
}
