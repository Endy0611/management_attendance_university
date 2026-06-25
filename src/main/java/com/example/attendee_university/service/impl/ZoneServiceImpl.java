package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.zones.request.ZoneRequest;
import com.example.attendee_university.model.dto.zones.response.ZoneResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.Zone;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.ZoneRepository;
import com.example.attendee_university.service.ZoneService;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final AppUserRepository appUserRepository;
    private final HandleCurrentUser handleCurrentUser;

    @Override
    @Transactional
    public ZoneResponse createZone(ZoneRequest request) {
        UUID currentUserId = handleCurrentUser.getUserIdOfCurrentUser();

        Zone zone = Zone.builder()
                .name(request.name())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .radiusMeters(request.radiusMeters())
                .createdBy(currentUserId)
                .build();

        Zone saved = zoneRepository.save(zone);
        log.info("Zone created: {} by user {}", saved.getName(), currentUserId);
        return toResponse(saved);
    }

    @Override
    public List<ZoneResponse> getAllZones() {
        return zoneRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ZoneResponse getZoneById(UUID id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public ZoneResponse updateZone(UUID id, ZoneRequest request) {
        Zone zone = findOrThrow(id);

        zone.setName(request.name());
        zone.setLatitude(request.latitude());
        zone.setLongitude(request.longitude());
        zone.setRadiusMeters(request.radiusMeters());
        // dirty checking saves automatically inside @Transactional

        log.info("Zone updated: {}", zone.getName());
        return toResponse(zone);
    }

    @Override
    @Transactional
    public void deleteZone(UUID id) {
        Zone zone = findOrThrow(id);
        zoneRepository.delete(zone);
        log.info("Zone deleted: {}", zone.getName());
    }

    // ── Helpers ────────────────────────────────────────────────
    private Zone findOrThrow(UUID id) {
        return zoneRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Zone not found."));
    }

    private ZoneResponse toResponse(Zone zone) {
        AppUser creator = appUserRepository.findById(zone.getCreatedBy()).orElse(null);
        return ZoneResponse.builder()
                .id(zone.getId())
                .name(zone.getName())
                .latitude(zone.getLatitude())
                .longitude(zone.getLongitude())
                .radiusMeters(zone.getRadiusMeters())
                .createdBy(zone.getCreatedBy())
                .createdByName(creator != null ? creator.getName() : null)
                .createdAt(zone.getCreatedAt())
                .build();
    }
}