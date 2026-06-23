package com.example.attendee_university.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "device_fingerprints")
public class DeviceFingerprint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    // one device per user
    @Column(name = "app_user_id", nullable = false, unique = true)
    private UUID appUserId;

    // a given physical device can only ever be bound to one account
    @Column(name = "fingerprint_hash", nullable = false, unique = true)
    private String fingerprintHash;

    @Column(name = "device_info")
    private String deviceInfo;

    @Column(name = "created_at", updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}