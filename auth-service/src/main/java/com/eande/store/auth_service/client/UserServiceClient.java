package com.eande.store.auth_service.client;
import com.eande.store.auth_service.dto.client.UserDto;
import com.eande.store.auth_service.dto.client.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;
import java.util.UUID;

@FeignClient(
        name = "user-service",
        url = "${services.user-service.url:http://localhost:8081}",
        fallbackFactory = UserServiceClientFallbackFactory.class
)
public interface UserServiceClient {

    /**
     * Get user by ID
     * @param userId UUID of the user
     * @return UserDto wrapped in Optional
     */
    @GetMapping("/api/v1/users/{userId}")
    Optional<UserDto> findById(@PathVariable("userId") UUID userId);

    /**
     * Get user by email
     * @param email Email address of the user
     * @return UserDto wrapped in Optional
     */
    @GetMapping("/api/v1/users/email/{email}")
    Optional<UserDto> findByEmail(@PathVariable("email") String email);

    /**
     * Check if user exists by email
     * @param email Email address to check
     * @return true if user exists and is active
     */
    @GetMapping("/api/v1/users/exists/{email}")
    boolean existsByEmail(@PathVariable("email") String email);

    /**
     * Validate user credentials (delegates to user-service)
     * @param email User email
     * @param password Raw password
     * @return Validation result
     */
    @GetMapping("/api/v1/users/validate")
    UserValidationResponse validateCredentials(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Password") String password
    );
}
