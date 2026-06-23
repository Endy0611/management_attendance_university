package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.DeviceFingerprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceFingerprintRepository extends JpaRepository<DeviceFingerprint, UUID> {

    Optional<DeviceFingerprint> findByAppUserId(UUID appUserId);

    Optional<DeviceFingerprint> findByFingerprintHash(String fingerprintHash);

    boolean existsByAppUserId(UUID appUserId);
}
