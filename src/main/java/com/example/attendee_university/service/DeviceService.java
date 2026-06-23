package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.device.request.BindDeviceRequest;
import com.example.attendee_university.model.dto.device.response.DeviceResponse;
import jakarta.validation.Valid;

public interface DeviceService {

    DeviceResponse bindDevice(@Valid BindDeviceRequest request);

    DeviceResponse getMyDevice();
}
