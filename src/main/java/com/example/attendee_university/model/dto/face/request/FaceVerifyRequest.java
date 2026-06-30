package com.example.attendee_university.model.dto.face.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record FaceVerifyRequest(

        @NotBlank(message = "Image data is required.")
        String imageBase64
) {}