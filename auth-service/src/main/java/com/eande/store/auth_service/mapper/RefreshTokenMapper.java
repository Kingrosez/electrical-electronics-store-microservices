package com.eande.store.auth_service.mapper;

import com.eande.store.auth_service.entity.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

    /**
     * Convert parameters to RefreshToken entity
     * Note: BaseEntity fields (createdAt, updatedAt, createdBy, updatedBy)
     * are handled by @PrePersist in the entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "revoked", constant = "false")
    @Mapping(target = "revokedAt", ignore = true)
    @Mapping(target = "revokedReason", ignore = true)
    // BaseEntity fields are ignored automatically due to unmappedTargetPolicy = IGNORE
    RefreshToken toEntity(
            UUID userId,
            String token,
            String ipAddress,
            String userAgent,
            Instant expiryDate
    );

    /**
     * Create refresh token with expiry calculated from seconds
     */
    default RefreshToken createRefreshToken(
            UUID userId,
            String token,
            String ipAddress,
            String userAgent,
            long expirySeconds) {

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(expirySeconds));
        refreshToken.setRevoked(false);
        // BaseEntity fields (createdAt, updatedAt) will be set by @PrePersist
        return refreshToken;
    }
}