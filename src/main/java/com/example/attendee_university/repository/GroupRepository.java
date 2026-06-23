package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByCourseId(UUID courseId);
    List<Group> findByInstructorId(UUID instructorId);
}