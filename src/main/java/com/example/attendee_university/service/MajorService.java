package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.major.request.MajorRequest;
import com.example.attendee_university.model.dto.major.response.MajorResponse;

import java.util.List;
import java.util.UUID;

public interface MajorService {
    MajorResponse createMajor(MajorRequest request);
    List<MajorResponse> getAllMajors();
    MajorResponse getMajorById(UUID id);
    MajorResponse updateMajor(UUID id, MajorRequest request);
    void deleteMajor(UUID id);
}