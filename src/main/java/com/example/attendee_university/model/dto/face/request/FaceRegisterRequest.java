package com.example.attendee_university.model.dto.face.request;

import jakarta.validation.constraints.NotBlank;

public record FaceRegisterRequest(

        @NotBlank(message = "Image data is required.")
        String imageBase64
) {}