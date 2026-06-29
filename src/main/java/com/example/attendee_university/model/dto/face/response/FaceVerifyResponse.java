package com.example.attendee_university.model.dto.face.response;

public record FaceVerifyResponse(
        boolean matched,
        double  similarity  // cosine similarity score from FastAPI (0.0–1.0)
) {}