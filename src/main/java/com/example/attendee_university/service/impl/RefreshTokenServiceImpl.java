package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.BadRequestException;
import com.example.attendee_university.jwt.JwtService;
import com.example.attendee_university.model.dto.auth.request.RefreshTokenRequest;
import com.example.attendee_university.model.dto.auth.response.AuthResponse;
import com.example.attendee_university.model.entity.AppUser;
import com.example.attendee_university.model.entity.RefreshToken;
import com.example.attendee_university.repository.AppUserRepository;
import com.example.attendee_university.repository.RefreshTokenRepository;
import com.example.attendee_university.service.RefreshTokenService;
import com.example.attendee_university.utils.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository      appUserRepository;
    private final TokenHashUtil          tokenHashUtil;
    private final JwtService             jwtService;

    @Value("${jwt.refresh-token-expiry-days:7}")
    private int refreshTokenExpiryDays;

    @Value("${jwt.remember-me-expiry-days:30}")
    private int rememberMeExpiryDays;

    // ── Issue new refresh token ───────────────────────────────
    @Override
    @Transactional
    public String issueToken(UUID appUserId, String deviceInfo, String ipAddress, boolean rememberMe) {
        String rawToken = tokenHashUtil.generateRawToken();
        int expiryDays  = rememberMe ? rememberMeExpiryDays : refreshTokenExpiryDays;

        RefreshToken token = RefreshToken.builder()
                .appUserId(appUserId)
                .tokenHash(tokenHashUtil.hash(rawToken))
                .expiresAt(Instant.now().plus(expiryDays, ChronoUnit.DAYS))
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(token);
        log.info("Refresh token issued for user: {} | rememberMe: {}", appUserId, rememberMe);
        return rawToken;
    }

    // ── Rotate token ──────────────────────────────────────────
    @Override
    @Transactional
    public AuthResponse rotate(RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String hash = tokenHashUtil.hash(request.getRefreshToken());

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token."));

        // token reuse detected — revoke all sessions
        if (existing.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(existing.getAppUserId());
            log.warn("Token reuse detected for user: {}. All sessions revoked.", existing.getAppUserId());
            throw new BadRequestException("Token reuse detected. All sessions revoked. Please log in again.");
        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.revokeById(existing.getTokenId());
            throw new BadRequestException("Refresh token expired. Please log in again.");
        }

        refreshTokenRepository.revokeById(existing.getTokenId());

        // preserve rememberMe duration
        long remainingDays = ChronoUnit.DAYS.between(Instant.now(), existing.getExpiresAt());
        boolean wasRememberMe = remainingDays > refreshTokenExpiryDays;
        int expiryDays = wasRememberMe ? rememberMeExpiryDays : refreshTokenExpiryDays;

        String newRaw = tokenHashUtil.generateRawToken();
        RefreshToken newToken = RefreshToken.builder()
                .appUserId(existing.getAppUserId())
                .tokenHash(tokenHashUtil.hash(newRaw))
                .expiresAt(Instant.now().plus(expiryDays, ChronoUnit.DAYS))
                .deviceInfo(httpRequest.getHeader("User-Agent"))
                .ipAddress(httpRequest.getRemoteAddr())
                .build();

        RefreshToken saved = refreshTokenRepository.save(newToken);
        refreshTokenRepository.linkReplacement(existing.getTokenId(), saved.getTokenId());

        AppUser user = appUserRepository.findById(existing.getAppUserId())
                .orElseThrow(() -> new BadRequestException("User not found."));

        String newAccessToken = jwtService.generateToken(user);
        log.info("Refresh token rotated for user: {}", existing.getAppUserId());
        return new AuthResponse(newAccessToken, newRaw, user.isFirstLogin());
    }

    // ── Revoke single token ───────────────────────────────────
    @Override
    @Transactional
    public void revokeToken(RefreshTokenRequest request) {
        String hash = tokenHashUtil.hash(request.getRefreshToken());

        RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Refresh token not found."));

        if (token.isRevoked()) {
            throw new BadRequestException("Refresh token already revoked.");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Refresh token has expired.");
        }

        refreshTokenRepository.revokeById(token.getTokenId());
        log.info("Refresh token revoked for user: {}", token.getAppUserId());
    }

    // ── Revoke all sessions ───────────────────────────────────
    @Override
    @Transactional
    public void revokeAllSessions(UUID appUserId) {
        int count = refreshTokenRepository.revokeAllByUserId(appUserId);
        log.info("Revoked {} session(s) for user: {}", count, appUserId);
    }
}