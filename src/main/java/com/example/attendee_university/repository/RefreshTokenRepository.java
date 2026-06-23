package com.example.attendee_university.repository;

import com.example.attendee_university.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true, t.updatedAt = CURRENT_TIMESTAMP WHERE t.tokenId = :tokenId")
    void revokeById(@Param("tokenId") UUID tokenId);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.replacedBy = :newTokenId, t.updatedAt = CURRENT_TIMESTAMP WHERE t.tokenId = :oldTokenId")
    void linkReplacement(@Param("oldTokenId") UUID oldTokenId, @Param("newTokenId") UUID newTokenId);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revoked = true, t.updatedAt = CURRENT_TIMESTAMP WHERE t.appUserId = :appUserId AND t.revoked = false")
    int revokeAllByUserId(@Param("appUserId") UUID appUserId);
}