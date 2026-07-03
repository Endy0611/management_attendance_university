package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.course.request.CourseRequest;
import com.example.attendee_university.model.dto.course.response.CourseResponse;
import com.example.attendee_university.service.CourseService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@RequestBody @Valid CourseRequest request) {
        return ApiResponse.created("Course created.", courseService.createCourse(request));
    }

    // ── List all courses (any authenticated user) ──────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getAllCourses() {
        return ApiResponse.success("Courses fetched.", courseService.getAllCourses());
    }

    // ── Get one course ──────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable UUID id) {
        return ApiResponse.success("Course fetched.", courseService.getCourseById(id));
    }

    // ── Update course (Admin) ───────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID id, @RequestBody @Valid CourseRequest request) {
        return ApiResponse.success("Course updated.", courseService.updateCourse(id, request));
    }

    // ── Delete course (Admin) ───────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponse<Void> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ApiResponse.ok("Course deleted.");
    }
}