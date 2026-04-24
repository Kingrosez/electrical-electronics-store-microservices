package com.eande.store.auth_service.client;
import com.eande.store.auth_service.dto.client.UserDto;
import com.eande.store.auth_service.dto.client.UserValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class UserServiceClientFallbackFactory implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("User Service fallback triggered. Cause: {}", cause.getMessage());

        return new UserServiceClient() {

            @Override
            public Optional<UserDto> findById(UUID userId) {
                log.warn("Fallback: Cannot fetch user by ID: {}. User service may be down.", userId);
                return Optional.empty();
            }

            @Override
            public Optional<UserDto> findByEmail(String email) {
                log.warn("Fallback: Cannot fetch user by email: {}. User service may be down.", email);
                return Optional.empty();
            }

            @Override
            public boolean existsByEmail(String email) {
                log.warn("Fallback: Cannot check existence for email: {}. Assuming false.", email);
                return false;
            }

            @Override
            public UserValidationResponse validateCredentials(String email, String password) {
                log.warn("Fallback: Cannot validate credentials for email: {}. User service may be down.", email);
                return UserValidationResponse.builder()
                        .valid(false)
                        .errorMessage("User service unavailable. Please try again later.")
                        .build();
            }
        };
    }
}
