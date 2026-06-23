package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.ForbiddenException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.device.request.BindDeviceRequest;
import com.example.attendee_university.model.dto.device.response.DeviceResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.DeviceFingerprint;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.DeviceFingerprintRepository;
import com.example.attendee_university.service.DeviceService;
import com.example.attendee_university.utils.HandleCurrentUser;
import com.example.attendee_university.utils.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private static final String DEVICE_DAILY_PREFIX = "device:daily:";

    private final DeviceFingerprintRepository deviceFingerprintRepository;
    private final AppUserRepository appUserRepository;
    private final HandleCurrentUser handleCurrentUser;
    private final TokenHashUtil tokenHashUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public DeviceResponse bindDevice(BindDeviceRequest request) {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found."));

        if (user.isDeviceBound()) {
            throw new BadRequestException(
                    "A device is already bound to this account. Contact an admin to reset it.");
        }

        String fingerprintHash = tokenHashUtil.hash(request.fingerprint());

        // ── Same-day cross-user check (Redis) ──────────────────
        // Blocks the same physical device from being bound by a *different*
        // user within the same day (e.g. shared/emulated device abuse).
        String dailyKey = DEVICE_DAILY_PREFIX + fingerprintHash + ":" + LocalDate.now();
        String lockedUserId = redisTemplate.opsForValue().get(dailyKey);
        if (lockedUserId != null && !lockedUserId.equals(userId.toString())) {
            log.warn("Device fingerprint reuse blocked: {} already used today by another user.", fingerprintHash);
            throw new ForbiddenException(
                    "This device was already used by another account today. Please try again later.");
        }

        // ── Permanent cross-user check (DB) ────────────────────
        // A given device may only ever be permanently bound to one account.
        deviceFingerprintRepository.findByFingerprintHash(fingerprintHash).ifPresent(existing -> {
            if (!existing.getAppUserId().equals(userId)) {
                throw new ForbiddenException("This device is already registered to another account.");
            }
        });

        DeviceFingerprint device = DeviceFingerprint.builder()
                .appUserId(userId)
                .fingerprintHash(fingerprintHash)
                .deviceInfo(request.deviceInfo())
                .build();

        DeviceFingerprint saved = deviceFingerprintRepository.save(device);

        redisTemplate.opsForValue().set(dailyKey, userId.toString(), Duration.ofDays(1));

        user.setDeviceBound(true);
        // dirty checking saves automatically inside @Transactional

        log.info("Device bound for user: {}", user.getEmail());
        return toResponse(saved);
    }

    // ── Get current user's bound device ───────────────────────
    @Override
    public DeviceResponse getMyDevice() {
        UUID userId = handleCurrentUser.getUserIdOfCurrentUser();
        DeviceFingerprint device = deviceFingerprintRepository.findByAppUserId(userId)
                .orElseThrow(() -> new NotFoundException("No device bound to this account yet."));
        return toResponse(device);
    }

    private DeviceResponse toResponse(DeviceFingerprint device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .deviceInfo(device.getDeviceInfo())
                .createdAt(device.getCreatedAt())
                .build();
    }
}
