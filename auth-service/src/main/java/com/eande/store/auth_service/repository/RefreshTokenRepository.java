package com.eande.store.auth_service.repository;

import com.eande.store.auth_service.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt, " +
            "rt.revokedReason = :reason WHERE rt.userId = :userId")
    void revokeAllUserTokens(@Param("userId") UUID userId,
                             @Param("revokedAt") Instant revokedAt,
                             @Param("reason") String reason);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :revokedAt, " +
            "rt.revokedReason = :reason WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token,
                       @Param("revokedAt") Instant revokedAt,
                       @Param("reason") String reason);

    default void revokeByToken(String token) {
        revokeByToken(token, Instant.now(), "User logged out");
    }

    long countByUserIdAndRevokedFalse(UUID userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.revoked = false " +
            "AND rt.expiryDate > :now ORDER BY rt.createdAt DESC")
    Optional<RefreshToken> findLatestValidToken(@Param("userId") UUID userId,
                                                @Param("now") Instant now);

    void deleteByExpiryDateBefore(Instant now);
}