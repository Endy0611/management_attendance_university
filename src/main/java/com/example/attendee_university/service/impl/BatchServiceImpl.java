package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.batch.request.BatchRequest;
import com.example.attendee_university.model.dto.batch.response.BatchResponse;
import com.example.attendee_university.model.entity.Batch;
import com.example.attendee_university.repository.BatchRepository;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public BatchResponse createBatch(BatchRequest request) {
        if (batchRepository.findAll().stream().anyMatch(b -> b.getName().equalsIgnoreCase(request.name()))) {
            throw new BadRequestException("Batch name already exists: " + request.name());
        }

        Batch batch = Batch.builder()
                .name(request.name())
                .intakeYear(request.intakeYear())
                .build();

        Batch saved = batchRepository.save(batch);
        log.info("Batch created: {}", saved.getName());
        return toResponse(saved);
    }

    @Override
    public List<BatchResponse> getAllBatches() {
        return batchRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BatchResponse getBatchById(UUID id) {
        Batch batch = findOrThrow(id);
        return toResponse(batch);
    }

    @Override
    @Transactional
    public BatchResponse updateBatch(UUID id, BatchRequest request) {
        Batch batch = findOrThrow(id);

        boolean nameTaken = batchRepository.findAll().stream()
                .anyMatch(b -> !b.getId().equals(id) && b.getName().equalsIgnoreCase(request.name()));
        if (nameTaken) {
            throw new BadRequestException("Batch name already exists: " + request.name());
        }

        batch.setName(request.name());
        batch.setIntakeYear(request.intakeYear());
        return toResponse(batch);
    }

    @Override
    @Transactional
    public void deleteBatch(UUID id) {
        Batch batch = findOrThrow(id);

        if (!groupRepository.findByBatchId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete a batch that still has groups. Delete its groups first.");
        }

        batchRepository.delete(batch);
        log.info("Batch deleted: {}", batch.getName());
    }

    private Batch findOrThrow(UUID id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Batch not found."));
    }

    private BatchResponse toResponse(Batch batch) {
        int groupCount = groupRepository.findByBatchId(batch.getId()).size();
        return BatchResponse.builder()
                .id(batch.getId())
                .name(batch.getName())
                .intakeYear(batch.getIntakeYear())
                .groupCount(groupCount)
                .createdAt(batch.getCreatedAt())
                .build();
    }
}