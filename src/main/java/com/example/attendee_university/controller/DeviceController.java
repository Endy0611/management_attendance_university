package com.example.attendee_university.controller;

import com.example.attendee_university.model.dto.ApiResponse;
import com.example.attendee_university.model.dto.device.request.BindDeviceRequest;
import com.example.attendee_university.model.dto.device.response.DeviceResponse;
import com.example.attendee_university.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "Bind a device fingerprint to the current user")
    @PostMapping("/bind")
    public ResponseEntity<ApiResponse<DeviceResponse>> bind(
            @RequestBody @Valid BindDeviceRequest request) {
        DeviceResponse response = deviceService.bindDevice(request);
        return ApiResponse.success("Device bound successfully.", response);
    }

    @Operation(summary = "Get the device bound to the current user")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<DeviceResponse>> getMyDevice() {
        return ApiResponse.success("Device fetched.", deviceService.getMyDevice());
    }
}