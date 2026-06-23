package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.course.request.CourseRequest;
import com.example.attendee_university.model.dto.course.response.CourseResponse;
import com.example.attendee_university.model.entity.Course;
import com.example.attendee_university.repository.CourseRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        if (courseRepository.existsByCode(request.code())) {
            throw new BadRequestException("Course code already exists: " + request.code());
        }

        Course course = Course.builder()
                .code(request.code())
                .name(request.name())
                .build();

        Course saved = courseRepository.save(course);
        log.info("Course created: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CourseResponse getCourseById(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found."));
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(UUID id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found."));

        if (!course.getCode().equals(request.code()) && courseRepository.existsByCode(request.code())) {
            throw new BadRequestException("Course code already exists: " + request.code());
        }

        course.setCode(request.code());
        course.setName(request.name());
        // dirty checking saves automatically inside @Transactional

        return toResponse(course);
    }

    @Override
    @Transactional
    public void deleteCourse(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found."));

        if (!groupRepository.findByCourseId(id).isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete a course that still has groups. Delete its groups first.");
        }

        courseRepository.delete(course);
        log.info("Course deleted: {}", course.getCode());
    }

    private CourseResponse toResponse(Course course) {
        int groupCount = groupRepository.findByCourseId(course.getId()).size();
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .groupCount(groupCount)
                .createdAt(course.getCreatedAt())
                .build();
    }
}