package com.example.attendee_university.model.dto.attendance.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record AttendanceCheckInRequest(

        @NotNull
        Double latitude,

        @NotNull
        Double longitude
) {
}
