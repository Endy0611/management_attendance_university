package com.example.attendee_university.service;

import java.util.UUID;

public interface AttendanceReportService {

    /** CSV of all students in a session (absentees included). */
    byte[] exportSessionCsv(UUID sessionId);

    /** CSV matrix: rows = students, columns = sessions. */
    byte[] exportGroupCsv(UUID groupId);
}