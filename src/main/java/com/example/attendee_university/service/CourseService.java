package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.course.request.CourseRequest;
import com.example.attendee_university.model.dto.course.response.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseResponse createCourse(CourseRequest request);

    List<CourseResponse> getAllCourses();

    CourseResponse getCourseById(UUID id);

    CourseResponse updateCourse(UUID id, CourseRequest request);

    void deleteCourse(UUID id);
}