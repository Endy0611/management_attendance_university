package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.zones.request.ZoneRequest;
import com.example.attendee_university.model.dto.zones.response.ZoneResponse;

import java.util.List;
import java.util.UUID;

public interface ZoneService {

    ZoneResponse createZone(ZoneRequest request);

    List<ZoneResponse> getAllZones();

    ZoneResponse getZoneById(UUID id);

    ZoneResponse updateZone(UUID id, ZoneRequest request);

    void deleteZone(UUID id);
}