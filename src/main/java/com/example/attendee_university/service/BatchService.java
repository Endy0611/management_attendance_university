package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.batch.request.BatchRequest;
import com.example.attendee_university.model.dto.batch.response.BatchResponse;

import java.util.List;
import java.util.UUID;

public interface BatchService {
    BatchResponse createBatch(BatchRequest request);
    List<BatchResponse> getAllBatches();
    BatchResponse getBatchById(UUID id);
    BatchResponse updateBatch(UUID id, BatchRequest request);
    void deleteBatch(UUID id);
}