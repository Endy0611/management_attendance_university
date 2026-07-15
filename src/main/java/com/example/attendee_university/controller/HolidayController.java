package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.holiday.request.HolidayRequest;
import com.example.attendee_university.model.dto.holiday.response.HolidayResponse;
import com.example.attendee_university.service.HolidayService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class HolidayController {

    private final HolidayService holidayService;

    // ── Create holiday (Admin) — auto-cancels/reschedules any already-
    //    generated future sessions that fall on this date ────────
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<HolidayResponse>> createHoliday(
            @RequestBody @Valid HolidayRequest request) {
        HolidayResponse response = holidayService.createHoliday(request);
        return ApiResponse.success("Holiday created. Any conflicting sessions were cancelled and rescheduled.", response);
    }

    // ── List all holidays (Admin, Instructor) ───────────────────
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'INSTRUCTOR')")
    public List<HolidayResponse> getAllHolidays() {
        return holidayService.getAllHolidays();
    }

    // ── Delete holiday (Admin) ───────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteHoliday(@PathVariable UUID id) {
        holidayService.deleteHoliday(id);
        return ApiResponse.ok("Holiday deleted.");
    }
}