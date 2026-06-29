package com.example.attendee_university.model.dto.face.request;

import jakarta.validation.constraints.NotBlank;

public record FaceVerifyRequest(

        @NotBlank(message = "Image data is required.")
        String imageBase64
) {}