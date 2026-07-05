package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.exception.NotFoundException;
import com.example.attendee_university.model.dto.face.request.FaceRegisterRequest;
import com.example.attendee_university.model.dto.face.request.FaceVerifyRequest;
import com.example.attendee_university.model.dto.face.response.FaceStatusResponse;
import com.example.attendee_university.model.dto.face.response.FaceVerifyResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.FaceEmbedding;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.FaceEmbeddingRepository;
import com.example.attendee_university.service.FaceService;
import com.example.attendee_university.utils.HandleCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaceServiceImpl implements FaceService {

    private final FaceEmbeddingRepository faceEmbeddingRepository;
    private final AppUserRepository       appUserRepository;
    private final HandleCurrentUser       handleCurrentUser;
    private final WebClient               webClient;

    @Value("${face-api.url}")
    private String faceApiUrl;
    
    @jakarta.annotation.PostConstruct
public void logFaceApiUrl() {
    log.info("Resolved face-api.url = [{}]", faceApiUrl);
}

    private static final double SIMILARITY_THRESHOLD = 0.80;

    // ── Register Face (STUDENT) ───────────────────────────────
    @Override
    @Transactional
    public FaceStatusResponse registerFace(FaceRegisterRequest request) {
        AppUser user = handleCurrentUser.getCurrentUser();

        List<Double> embedding = extractEmbedding(request.imageBase64());
        String vector = serializeVector(embedding);

        FaceEmbedding faceEmbedding = faceEmbeddingRepository.findByUserId(user.getId())
                .orElse(FaceEmbedding.builder().userId(user.getId()).build());

        faceEmbedding.setEmbeddingVector(vector);
        FaceEmbedding saved = faceEmbeddingRepository.save(faceEmbedding);

        user.setFaceRegistered(true);
        appUserRepository.save(user);

        log.info("Face registered for user: {}", user.getEmail());
        return new FaceStatusResponse(user.getId(), true, saved.getRegisteredAt());
    }

    // ── Verify Face (STUDENT) ─────────────────────────────────
    @Override
    public FaceVerifyResponse verifyFace(FaceVerifyRequest request) {
        AppUser user = handleCurrentUser.getCurrentUser();

        FaceEmbedding stored = faceEmbeddingRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException(
                        "No face registered. Please register your face first."));

        List<Double> liveEmbedding   = extractEmbedding(request.imageBase64());
        List<Double> storedEmbedding = deserializeVector(stored.getEmbeddingVector());

        double similarity = cosineSimilarity(liveEmbedding, storedEmbedding);
        boolean matched   = similarity >= SIMILARITY_THRESHOLD;

        log.info("Face verify for {}: similarity={} matched={}", user.getEmail(), similarity, matched);
        return new FaceVerifyResponse(matched, similarity);
    }

    // ── Get my face status ────────────────────────────────────
    @Override
    public FaceStatusResponse getMyFaceStatus() {
        AppUser user = handleCurrentUser.getCurrentUser();
        return faceEmbeddingRepository.findByUserId(user.getId())
                .map(fe -> new FaceStatusResponse(user.getId(), true, fe.getRegisteredAt()))
                .orElse(new FaceStatusResponse(user.getId(), false, null));
    }

    // ── ADMIN: Reset face ─────────────────────────────────────
    @Override
    @Transactional
    public void adminResetFace(UUID userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        faceEmbeddingRepository.deleteByUserId(userId);
        user.setFaceRegistered(false);
        appUserRepository.save(user);

        log.info("Face registration reset by admin for user: {}", user.getEmail());
    }

    // ── Call FastAPI /embed ───────────────────────────────────
    @SuppressWarnings("unchecked")
    private List<Double> extractEmbedding(String imageBase64) {
        try {
            Map<String, Object> result = webClient.post()
                    .uri(faceApiUrl + "/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("image_base64", imageBase64))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (result == null || !result.containsKey("embedding")) {
                throw new BadRequestException("Face extraction failed: no embedding returned.");
            }
            return (List<Double>) result.get("embedding");

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("FastAPI embedding error: {}", e.getMessage());
            throw new BadRequestException(
                    "Could not extract face embedding. Ensure your face is clearly visible.");
        }
    }

    // ── Cosine similarity ─────────────────────────────────────
    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            throw new BadRequestException("Embedding dimension mismatch.");
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.size(); i++) {
            dot   += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private String serializeVector(List<Double> v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(v.get(i));
        }
        return sb.toString();
    }

    private List<Double> deserializeVector(String v) {
        return Arrays.stream(v.split(","))
                .map(Double::parseDouble)
                .toList();
    }
}
