package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.constraint.AttendanceStatus;
import com.example.attendee_university.model.entity.*;
import com.example.attendee_university.repository.*;
import com.example.attendee_university.service.AttendanceReportService;
import com.example.attendee_university.utils.AppTimeZone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceReportServiceImpl implements AttendanceReportService {

    private final GroupSessionRepository groupSessionRepository;
    private final GroupRepository        groupRepository;
    private final GroupMemberRepository  groupMemberRepository;
    private final AttendanceRepository   attendanceRepository;
    private final AppUserRepository      appUserRepository;
    private final CourseRepository       courseRepository;

    // Instant has no local fields of its own, so the formatter needs an explicit
    // zone to render CSV timestamps in Cambodia local time instead of throwing.
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(AppTimeZone.CAMBODIA);

    // ── Single session CSV ────────────────────────────────────
    @Override
    public byte[] exportSessionCsv(UUID sessionId) {
        GroupSession session = groupSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));

        Group  group      = groupRepository.findById(session.getGroupId()).orElse(null);
        String groupName  = group != null ? group.getName() : "N/A";
        String courseCode = resolveCourseCode(group);

        StringBuilder sb = new StringBuilder();
        sb.append("Session ID,Group,Course,Start Time,End Time\n");
        sb.append(csv(sessionId.toString())).append(",")
                .append(csv(groupName)).append(",")
                .append(csv(courseCode)).append(",")
                .append(FMT.format(session.getStartTime())).append(",")
                .append(FMT.format(session.getEndTime())).append("\n\n");

        sb.append("No,Student Name,Student ID,Email,Status,Checked-In At,Distance (m)\n");

        List<GroupMember> members = groupMemberRepository.findByGroupId(session.getGroupId());
        Map<UUID, AttendanceRecord> attendanceMap = buildAttendanceMap(sessionId);

        int i = 1;
        for (GroupMember member : members) {
            AppUser student = appUserRepository.findById(member.getAppUserId()).orElse(null);
            if (student == null) continue;

            AttendanceRecord record   = attendanceMap.get(student.getId());
            String           status   = record != null ? record.getStatus().name() : AttendanceStatus.ABSENT.name();
            String           checkedIn = record != null ? FMT.format(record.getCheckedInAt()) : "-";
            String           distance  = record != null ? String.format("%.1f", record.getDistanceMeters()) : "-";

            sb.append(i++).append(",")
                    .append(csv(student.getName())).append(",")
                    .append(nullSafe(student.getStudentId())).append(",")
                    .append(student.getEmail()).append(",")
                    .append(status).append(",")
                    .append(checkedIn).append(",")
                    .append(distance).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── Full group CSV matrix ─────────────────────────────────
    @Override
    public byte[] exportGroupCsv(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found."));

        List<GroupSession> sessions = groupSessionRepository.findByGroupId(groupId);
        List<GroupMember>  members  = groupMemberRepository.findByGroupId(groupId);

        StringBuilder sb = new StringBuilder();
        sb.append("Group: ").append(group.getName()).append("\n");
        sb.append("Course: ").append(resolveCourseCode(group)).append("\n\n");

        // Header row
        sb.append("Student Name,Student ID,Email");
        for (GroupSession s : sessions) {
            sb.append(",").append(FMT.format(s.getStartTime()));
        }
        sb.append(",Total Present,Total Late,Total Absent\n");

        // Data rows
        for (GroupMember member : members) {
            AppUser student = appUserRepository.findById(member.getAppUserId()).orElse(null);
            if (student == null) continue;

            sb.append(csv(student.getName())).append(",")
                    .append(nullSafe(student.getStudentId())).append(",")
                    .append(student.getEmail());

            int present = 0, late = 0, absent = 0;
            for (GroupSession s : sessions) {
                AttendanceStatus status = attendanceRepository
                        .findBySessionIdAndStudentId(s.getId(), student.getId())
                        .map(AttendanceRecord::getStatus)
                        .orElse(AttendanceStatus.ABSENT);
                sb.append(",").append(status.name());
                if (status == AttendanceStatus.PRESENT)      present++;
                else if (status == AttendanceStatus.LATE)    late++;
                else                                          absent++;
            }
            sb.append(",").append(present)
                    .append(",").append(late)
                    .append(",").append(absent)
                    .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    // ── Helpers ───────────────────────────────────────────────
    private Map<UUID, AttendanceRecord> buildAttendanceMap(UUID sessionId) {
        Map<UUID, AttendanceRecord> map = new HashMap<>();
        attendanceRepository.findBySessionId(sessionId)
                .forEach(r -> map.put(r.getStudentId(), r));
        return map;
    }

    private String resolveCourseCode(Group group) {
        if (group == null) return "N/A";
        return courseRepository.findById(group.getCourseId())
                .map(c -> c.getCode())
                .orElse("N/A");
    }

    private String csv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    private String nullSafe(String v) { return v != null ? v : ""; }
}