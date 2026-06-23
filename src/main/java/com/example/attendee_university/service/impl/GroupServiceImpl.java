package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.ForbiddenException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.RoleType;
import com.example.attendee_university.model.dto.group.request.AddGroupMembersRequest;
import com.example.attendee_university.model.dto.group.request.GroupRequest;
import com.example.attendee_university.model.dto.group.response.GroupMemberResponse;
import com.example.attendee_university.model.dto.group.response.GroupResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.Course;
import com.example.attendee_university.model.entity.Group;
import com.example.attendee_university.model.entity.GroupMember;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.CourseRepository;
import com.example.attendee_university.repository.GroupMemberRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.service.GroupService;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CourseRepository courseRepository;
    private final AppUserRepository appUserRepository;
    private final HandleCurrentUser handleCurrentUser;

    // ── Create group (Admin) ───────────────────────────────────
    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found."));

        AppUser instructor = appUserRepository.findById(request.instructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found."));
        if (instructor.getRole() != RoleType.INSTRUCTOR) {
            throw new BadRequestException("Assigned user is not an instructor.");
        }

        Group group = Group.builder()
                .courseId(course.getId())
                .name(request.name())
                .instructorId(instructor.getId())
                .capacity(request.capacity())
                .semester(request.semester())
                .build();

        Group saved = groupRepository.save(group);
        log.info("Group created: {} under course {}", saved.getName(), course.getCode());
        return toResponse(saved);
    }

    // ── List all groups (Admin) ────────────────────────────────
    @Override
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GroupResponse getGroupById(UUID id) {
        Group group = findGroupOrThrow(id);
        assertCanManageGroup(group);
        return toResponse(group);
    }

    // ── Update group (Admin) ───────────────────────────────────
    @Override
    @Transactional
    public GroupResponse updateGroup(UUID id, GroupRequest request) {
        Group group = findGroupOrThrow(id);

        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found."));

        AppUser instructor = appUserRepository.findById(request.instructorId())
                .orElseThrow(() -> new NotFoundException("Instructor not found."));
        if (instructor.getRole() != RoleType.INSTRUCTOR) {
            throw new BadRequestException("Assigned user is not an instructor.");
        }

        long currentMemberCount = groupMemberRepository.countByGroupId(id);
        if (request.capacity() < currentMemberCount) {
            throw new BadRequestException(
                    "New capacity (" + request.capacity() + ") is less than current member count (" + currentMemberCount + ").");
        }

        group.setCourseId(course.getId());
        group.setName(request.name());
        group.setInstructorId(instructor.getId());
        group.setCapacity(request.capacity());
        group.setSemester(request.semester());
        // dirty checking saves automatically inside @Transactional

        return toResponse(group);
    }

    // ── Delete group (Admin) ───────────────────────────────────
    @Override
    @Transactional
    public void deleteGroup(UUID id) {
        Group group = findGroupOrThrow(id);
        groupMemberRepository.deleteAllByGroupId(id);
        groupRepository.delete(group);
        log.info("Group deleted: {}", group.getName());
    }

    // ── Groups for the current user ────────────────────────────
    @Override
    public List<GroupResponse> getMyGroups() {
        AppUser currentUser = handleCurrentUser.getCurrentUser();

        return switch (currentUser.getRole()) {
            case ADMIN -> getAllGroups();
            case INSTRUCTOR -> groupRepository.findByInstructorId(currentUser.getId()).stream()
                    .map(this::toResponse)
                    .toList();
            case STUDENT -> groupMemberRepository.findByAppUserId(currentUser.getId()).stream()
                    .map(member -> groupRepository.findById(member.getGroupId()).orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .map(this::toResponse)
                    .toList();
        };
    }

    // ── Add members (Admin, or the owning Instructor) ──────────
    @Override
    @Transactional
    public List<GroupMemberResponse> addMembers(UUID groupId, AddGroupMembersRequest request) {
        Group group = findGroupOrThrow(groupId);
        assertCanManageGroup(group);

        long currentCount = groupMemberRepository.countByGroupId(groupId);
        long incoming = request.studentIds().stream().distinct().count();

        if (currentCount + incoming > group.getCapacity()) {
            throw new BadRequestException(
                    "Adding these students would exceed the group's capacity (" + group.getCapacity() + ").");
        }

        List<GroupMemberResponse> result = new java.util.ArrayList<>();

        for (UUID studentId : request.studentIds().stream().distinct().toList()) {
            AppUser student = appUserRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));

            if (student.getRole() != RoleType.STUDENT) {
                throw new BadRequestException(student.getEmail() + " is not a student account.");
            }

            if (groupMemberRepository.existsByGroupIdAndAppUserId(groupId, studentId)) {
                throw new BadRequestException(student.getEmail() + " is already a member of this group.");
            }

            GroupMember member = GroupMember.builder()
                    .groupId(groupId)
                    .appUserId(studentId)
                    .build();

            GroupMember saved = groupMemberRepository.save(member);
            result.add(toMemberResponse(saved, student));
        }

        log.info("Added {} student(s) to group {}", result.size(), group.getName());
        return result;
    }

    // ── Remove member (Admin, or the owning Instructor) ────────
    @Override
    @Transactional
    public void removeMember(UUID groupId, UUID studentId) {
        Group group = findGroupOrThrow(groupId);
        assertCanManageGroup(group);

        if (!groupMemberRepository.existsByGroupIdAndAppUserId(groupId, studentId)) {
            throw new NotFoundException("Student is not a member of this group.");
        }

        groupMemberRepository.deleteByGroupIdAndAppUserId(groupId, studentId);
        log.info("Removed student {} from group {}", studentId, group.getName());
    }

    // ── List members (Admin, or the owning Instructor) ─────────
    @Override
    public List<GroupMemberResponse> getMembers(UUID groupId) {
        Group group = findGroupOrThrow(groupId);
        assertCanManageGroup(group);

        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(member -> {
                    AppUser student = appUserRepository.findById(member.getAppUserId()).orElse(null);
                    return toMemberResponse(member, student);
                })
                .toList();
    }

    // ── Helpers ─────────────────────────────────────────────────
    private Group findGroupOrThrow(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found."));
    }

    // Admin can manage any group; an Instructor can only manage their own.
    private void assertCanManageGroup(Group group) {
        AppUser currentUser = handleCurrentUser.getCurrentUser();
        if (currentUser.getRole() == RoleType.ADMIN) return;

        if (currentUser.getRole() == RoleType.INSTRUCTOR
                && group.getInstructorId().equals(currentUser.getId())) {
            return;
        }

        throw new ForbiddenException("You do not have permission to manage this group.");
    }

    private GroupResponse toResponse(Group group) {
        Course course = courseRepository.findById(group.getCourseId()).orElse(null);
        AppUser instructor = appUserRepository.findById(group.getInstructorId()).orElse(null);
        long memberCount = groupMemberRepository.countByGroupId(group.getId());

        return GroupResponse.builder()
                .id(group.getId())
                .courseId(group.getCourseId())
                .courseCode(course != null ? course.getCode() : null)
                .name(group.getName())
                .instructorId(group.getInstructorId())
                .instructorName(instructor != null ? instructor.getName() : null)
                .capacity(group.getCapacity())
                .memberCount((int) memberCount)
                .semester(group.getSemester())
                .createdAt(group.getCreatedAt())
                .build();
    }

    private GroupMemberResponse toMemberResponse(GroupMember member, AppUser student) {
        return GroupMemberResponse.builder()
                .studentId(member.getAppUserId())
                .studentName(student != null ? student.getName() : null)
                .studentEmail(student != null ? student.getEmail() : null)
                .studentNumber(student != null ? student.getStudentId() : null)
                .joinedAt(member.getJoinedAt())
                .build();
    }
}