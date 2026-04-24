package com.eande.store.auth_service.mapper;

import com.eande.store.auth_service.entity.LoginAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginAttemptMapper {

    /**
     * Convert parameters to LoginAttempt entity
     * Note: BaseEntity fields (createdAt, updatedAt, createdBy, updatedBy)
     * are handled by @PrePersist in the entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attemptedAt", source = "attemptedAt")
    // BaseEntity fields are ignored automatically due to unmappedTargetPolicy = IGNORE
    LoginAttempt toEntity(
            String email,
            String ipAddress,
            boolean success,
            String failureReason,
            Instant attemptedAt
    );

    /**
     * Factory method for failed attempt
     */
    default LoginAttempt toFailedAttempt(String email, String ipAddress, String failureReason) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(false);
        attempt.setFailureReason(failureReason);
        attempt.setAttemptedAt(Instant.now());
        // BaseEntity fields (createdAt, updatedAt) will be set by @PrePersist
        return attempt;
    }

    /**
     * Factory method for successful attempt
     */
    default LoginAttempt toSuccessfulAttempt(String email, String ipAddress) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccess(true);
        attempt.setFailureReason(null);
        attempt.setAttemptedAt(Instant.now());
        // BaseEntity fields (createdAt, updatedAt) will be set by @PrePersist
        return attempt;
    }
}