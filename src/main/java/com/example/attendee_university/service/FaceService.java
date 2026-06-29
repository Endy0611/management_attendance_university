package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.face.request.FaceRegisterRequest;
import com.example.attendee_university.model.dto.face.request.FaceVerifyRequest;
import com.example.attendee_university.model.dto.face.response.FaceStatusResponse;
import com.example.attendee_university.model.dto.face.response.FaceVerifyResponse;

import java.util.UUID;

public interface FaceService {

    FaceStatusResponse registerFace(FaceRegisterRequest request);

    FaceVerifyResponse verifyFace(FaceVerifyRequest request);

    FaceStatusResponse getMyFaceStatus();

    void adminResetFace(UUID userId);
}