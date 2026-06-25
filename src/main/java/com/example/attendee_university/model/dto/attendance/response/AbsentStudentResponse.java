package com.example.attendee_university.model.dto.attendance.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AbsentStudentResponse(

        UUID studentId,

        String studentName,

        String studentEmail,

        String studentNumber
) {}