package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.timetable.request.TimetableSlotRequest;
import com.example.attendee_university.model.dto.timetable.response.TimetableSlotResponse;

import java.util.List;
import java.util.UUID;

public interface TimetableSlotService {

    // creates the recurring slot AND generates its GroupSession occurrences
    TimetableSlotResponse createSlot(TimetableSlotRequest request);

    TimetableSlotResponse updateSlot(UUID id, TimetableSlotRequest request);

    void deleteSlot(UUID id);

    TimetableSlotResponse getSlotById(UUID id);

    // full weekly timetable for a batch/group, ordered like a real timetable (Mon -> Sun)
    List<TimetableSlotResponse> getWeeklyTimetableForGroup(UUID groupId);

    // role-aware: admin -> all, instructor -> slots they teach, student -> slots for groups they're enrolled in
    List<TimetableSlotResponse> getMyWeeklyTimetable();

    // (re)generate GroupSession occurrences for a slot; idempotent, skips ones already generated
    int generateSessions(UUID slotId);
}
