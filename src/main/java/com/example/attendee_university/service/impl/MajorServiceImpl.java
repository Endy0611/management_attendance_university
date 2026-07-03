package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.major.request.MajorRequest;
import com.example.attendee_university.model.dto.major.response.MajorResponse;
import com.example.attendee_university.model.entity.Major;
import com.example.attendee_university.repository.GroupRepository;
import com.example.attendee_university.repository.MajorRepository;
import com.example.attendee_university.service.MajorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MajorServiceImpl implements MajorService {

    private final MajorRepository majorRepository;
    private final GroupRepository groupRepository;

    @Override
    @Transactional
    public MajorResponse createMajor(MajorRequest request) {
        if (majorRepository.findAll().stream().anyMatch(m -> m.getCode().equalsIgnoreCase(request.code()))) {
            throw new BadRequestException("Major code already exists: " + request.code());
        }

        Major major = Major.builder()
                .name(request.name())
                .code(request.code())
                .build();

        Major saved = majorRepository.save(major);
        log.info("Major created: {}", saved.getCode());
        return toResponse(saved);
    }

    @Override
    public List<MajorResponse> getAllMajors() {
        return majorRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public MajorResponse getMajorById(UUID id) {
        Major major = findOrThrow(id);
        return toResponse(major);
    }

    @Override
    @Transactional
    public MajorResponse updateMajor(UUID id, MajorRequest request) {
        Major major = findOrThrow(id);

        boolean codeTaken = majorRepository.findAll().stream()
                .anyMatch(m -> !m.getId().equals(id) && m.getCode().equalsIgnoreCase(request.code()));
        if (codeTaken) {
            throw new BadRequestException("Major code already exists: " + request.code());
        }

        major.setName(request.name());
        major.setCode(request.code());
        return toResponse(major);
    }

    @Override
    @Transactional
    public void deleteMajor(UUID id) {
        Major major = findOrThrow(id);

        if (!groupRepository.findByMajorId(id).isEmpty()) {
            throw new BadRequestException("Cannot delete a major that still has groups. Delete its groups first.");
        }

        majorRepository.delete(major);
        log.info("Major deleted: {}", major.getCode());
    }

    private Major findOrThrow(UUID id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Major not found."));
    }

    private MajorResponse toResponse(Major major) {
        int groupCount = groupRepository.findByMajorId(major.getId()).size();
        return MajorResponse.builder()
                .id(major.getId())
                .name(major.getName())
                .code(major.getCode())
                .groupCount(groupCount)
                .createdAt(major.getCreatedAt())
                .build();
    }
}