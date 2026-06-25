package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.attendance.request.AttendanceCheckInRequest;
import com.example.attendee_university.model.dto.attendance.response.AbsentStudentResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceResponse;
import com.example.attendee_university.model.dto.attendance.response.AttendanceSummaryResponse;
import com.example.attendee_university.model.dto.attendance.response.StudentAttendanceResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceResponse checkIn(UUID sessionId, AttendanceCheckInRequest request);

    List<AttendanceResponse> getSessionAttendance(UUID sessionId);

    List<StudentAttendanceResponse> getMyAttendance();

    AttendanceSummaryResponse getSessionSummary(UUID sessionId);

    // ── New ───────────────────────────────────────────────────
    List<AbsentStudentResponse> getAbsentStudents(UUID sessionId);

    AttendanceResponse manualOverride(UUID sessionId, UUID studentId, String status);

    List<StudentAttendanceResponse> getSessionHistoryForMyGroups();
}