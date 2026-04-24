package com.eande.store.auth_service.repository;

import com.eande.store.auth_service.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, UUID> {

    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email " +
            "AND la.success = false AND la.attemptedAt > :since")
    long countFailedAttemptsSince(@Param("email") String email,
                                  @Param("since") Instant since);

    @Query("SELECT MAX(la.attemptedAt) FROM LoginAttempt la WHERE la.email = :email AND la.success = true")
    Instant getLastSuccessfulLogin(@Param("email") String email);

    void deleteByAttemptedAtBefore(Instant expiryTime);
}