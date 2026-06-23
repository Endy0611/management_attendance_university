package com.example.attendee_university.service;

import com.example.attendee_university.model.dto.auth.request.RefreshTokenRequest;
import com.example.attendee_university.model.dto.auth.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface RefreshTokenService {
    String issueToken(UUID appUserId, String deviceInfo, String ipAddress, boolean rememberMe);

    AuthResponse rotate(RefreshTokenRequest request, HttpServletRequest httpRequest);

    void revokeToken(RefreshTokenRequest request);

    void revokeAllSessions(UUID appUserId);
}
