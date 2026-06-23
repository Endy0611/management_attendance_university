package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.course.request.CourseRequest;
import com.example.attendee_university.model.dto.course.response.CourseResponse;
import com.example.attendee_university.service.CourseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;

    // ── Create course (Admin) ──────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<CourseResponse> createCourse(@RequestBody @Valid CourseRequest request) {
        CourseResponse response = courseService.createCourse(request);
        return ApiResponse.<CourseResponse>builder()
                .success(true)
                .message("Course created.")
                .status(org.springframework.http.HttpStatus.CREATED)
                .payload(response)
                .timestamp(java.time.Instant.now())
                .build();
    }

    // ── List all courses (any authenticated user) ──────────────
    @GetMapping
    public List<CourseResponse> getAllCourses() {
        return courseService.getAllCourses();
    }

    // ── Get one course ──────────────────────────────────────────
    @GetMapping("/{id}")
    public CourseResponse getCourse(@PathVariable UUID id) {
        return courseService.getCourseById(id);
    }

    // ── Update course (Admin) ───────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CourseResponse updateCourse(@PathVariable UUID id, @RequestBody @Valid CourseRequest request) {
        return courseService.updateCourse(id, request);
    }

    // ── Delete course (Admin) ───────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ApiResponse.ok("Course deleted.");
    }
}