package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.holiday.request.HolidayRequest;
import com.example.attendee_university.model.dto.holiday.response.HolidayResponse;

import java.util.List;
import java.util.UUID;

public interface HolidayService {

    HolidayResponse createHoliday(HolidayRequest request);

    List<HolidayResponse> getAllHolidays();

    void deleteHoliday(UUID id);
}