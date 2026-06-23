package com.example.attendee_university.model.dto.device.request;

import jakarta.validation.constraints.NotBlank;

public record BindDeviceRequest(

        @NotBlank(message = "Device fingerprint is required")
        String fingerprint,

        @NotBlank(message = "Device info is required")
        String deviceInfo
) {}