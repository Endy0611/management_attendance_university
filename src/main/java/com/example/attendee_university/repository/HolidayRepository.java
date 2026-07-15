package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    boolean existsByDate(LocalDate date);

    List<Holiday> findAllByOrderByDateAsc();

    List<Holiday> findAllByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}