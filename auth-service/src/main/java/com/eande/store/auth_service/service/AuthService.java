package com.eande.store.auth_service.service;

import com.eande.store.auth_service.client.UserServiceClient;
import com.eande.store.auth_service.dto.client.UserDto;
import com.eande.store.auth_service.dto.client.UserValidationResponse;
import com.eande.store.auth_service.dto.request.LoginRequest;
import com.eande.store.auth_service.dto.request.RefreshTokenRequest;
import com.eande.store.auth_service.dto.response.TokenResponse;
import com.eande.store.auth_service.entity.LoginAttempt;
import com.eande.store.auth_service.entity.RefreshToken;
import com.eande.store.auth_service.exception.*;
import com.eande.store.auth_service.mapper.LoginAttemptMapper;
import com.eande.store.auth_service.mapper.RefreshTokenMapper;
import com.eande.store.auth_service.repository.LoginAttemptRepository;
import com.eande.store.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final JwtService jwtService;

    // Mappers
    private final RefreshTokenMapper refreshTokenMapper;
    private final LoginAttemptMapper loginAttemptMapper;

    @Value("${auth.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${auth.account-lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    /**
     * Authenticate user and generate tokens
     */
    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Check for brute force
        checkBruteForceProtection(request.getEmail());

        // Fetch user from User Service
        UserDto user = userServiceClient.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", request.getEmail());
                    saveFailedAttempt(request.getEmail(), ipAddress, "USER_NOT_FOUND");
                    return AuthenticationException.invalidCredentials();
                });

        // Check if account is blocked
        if ("BLOCKED".equals(user.getStatus())) {
            log.warn("Login failed - account blocked for user: {}", user.getEmail());
            throw AuthenticationException.accountLocked();
        }

        // Check if account is inactive
        if (!user.isEnabled()) {
            log.warn("Login failed - account inactive for user: {}", user.getEmail());
            throw AuthenticationException.accountInactive();
        }

        // Check if account is deleted
        if (user.isDeleted()) {
            log.warn("Login failed - account deleted for user: {}", user.getEmail());
            throw AuthenticationException.accountDeleted();
        }

        // Verify password using User Service validation
        UserValidationResponse validationResponse = userServiceClient.validateCredentials(
                request.getEmail(), request.getPassword()
        );

        if (!validationResponse.isValid()) {
            log.warn("Login failed - invalid password for user: {}", user.getId());
            handleFailedPassword(user, ipAddress);
            saveFailedAttempt(request.getEmail(), ipAddress, "INVALID_PASSWORD");
            throw AuthenticationException.invalidCredentials();
        }

        // Success - save successful attempt
        saveSuccessfulAttempt(user.getEmail(), ipAddress);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = generateAndStoreRefreshToken(user, ipAddress, userAgent);

        log.info("Login successful for user: {}", user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .refreshExpiresIn(jwtService.getRefreshTokenExpirationSeconds())
                .build();
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        log.debug("Refresh token request received");

        String refreshTokenString = request.getRefreshToken();

        // Validate token structure
        if (!jwtService.isTokenValid(refreshTokenString)) {
            log.warn("Invalid refresh token structure");
            throw TokenException.invalidToken();
        }

        // Check token type
        String tokenType;
        try {
            tokenType = jwtService.extractTokenType(refreshTokenString);
        } catch (Exception e) {
            log.warn("Failed to extract token type");
            throw TokenException.invalidToken();
        }

        if (!"refresh".equals(tokenType)) {
            log.warn("Token type mismatch - expected refresh, got: {}", tokenType);
            throw TokenException.wrongTokenType();
        }

        String userIdStr;
        try {
            userIdStr = jwtService.extractUserId(refreshTokenString);
        } catch (Exception e) {
            log.warn("Failed to extract user ID from token");
            throw TokenException.invalidToken();
        }

        UUID userId = UUID.fromString(userIdStr);

        // Find stored refresh token
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenString)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found or revoked for user: {}", userId);
                    return TokenException.refreshTokenRevoked();
                });

        // Check expiry
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user: {}", userId);
            refreshTokenRepository.revokeByToken(refreshTokenString);
            throw TokenException.refreshTokenExpired();
        }

        // Fetch user from User Service
        UserDto user = userServiceClient.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during refresh: {}", userId);
                    return ResourceNotFoundException.userNotFoundById(userId.toString());
                });

        // Check if user is still active
        if (!user.isEnabled() || user.isDeleted()) {
            log.warn("User is no longer active during refresh: {}", userId);
            throw AuthenticationException.accountInactive();
        }

        // Revoke old token (token rotation)
        refreshTokenRepository.revokeByToken(refreshTokenString);
        log.debug("Old refresh token revoked for user: {}", userId);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = generateAndStoreRefreshToken(user, ipAddress, userAgent);

        log.info("Token refresh successful for user: {}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .refreshExpiresIn(jwtService.getRefreshTokenExpirationSeconds())
                .build();
    }

    /**
     * Logout - invalidate refresh token
     */
    @Transactional
    public void logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header for logout");
            return;
        }

        String token = authHeader.substring(7);

        try {
            String tokenType = jwtService.extractTokenType(token);
            if ("refresh".equals(tokenType)) {
                refreshTokenRepository.revokeByToken(token);
                log.debug("Refresh token revoked during logout");
            }
        } catch (Exception e) {
            log.warn("Error during logout: {}", e.getMessage());
        }

        log.info("Logout successful");
    }

    /**
     * Validate access token
     */
    public boolean validateToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }

        String token = authHeader.substring(7);

        try {
            String tokenType = jwtService.extractTokenType(token);
            if (!"access".equals(tokenType)) {
                return false;
            }

            boolean isValid = jwtService.isTokenValid(token);

            if (isValid) {
                String userIdStr = jwtService.extractUserId(token);
                log.debug("Token validation successful for user: {}", userIdStr);
            }

            return isValid;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate and store refresh token using mapper
     */
    private String generateAndStoreRefreshToken(UserDto user, String ipAddress, String userAgent) {
        String refreshTokenString = jwtService.generateRefreshToken(user.getId());

        // Using mapper to create RefreshToken entity
        RefreshToken refreshToken = refreshTokenMapper.createRefreshToken(
                user.getId(),
                refreshTokenString,
                ipAddress,
                userAgent,
                jwtService.getRefreshTokenExpirationSeconds()
        );

        refreshTokenRepository.save(refreshToken);

        return refreshTokenString;
    }

    /**
     * Check brute force protection
     */
    private void checkBruteForceProtection(String email) {
        Instant since = Instant.now().minus(lockoutDurationMinutes, ChronoUnit.MINUTES);
        long failedAttempts = loginAttemptRepository.countFailedAttemptsSince(email, since);

        if (failedAttempts >= maxFailedAttempts) {
            log.warn("Rate limit exceeded for email: {}", email);
            throw RateLimitException.tooManyLoginAttempts(lockoutDurationMinutes * 60L);
        }
    }

    /**
     * Save failed login attempt using mapper
     */
    private void saveFailedAttempt(String email, String ipAddress, String reason) {
        LoginAttempt attempt = loginAttemptMapper.toFailedAttempt(email, ipAddress, reason);
        loginAttemptRepository.save(attempt);
    }

    /**
     * Save successful login attempt using mapper
     */
    private void saveSuccessfulAttempt(String email, String ipAddress) {
        LoginAttempt attempt = loginAttemptMapper.toSuccessfulAttempt(email, ipAddress);
        loginAttemptRepository.save(attempt);
    }

    /**
     * Handle failed password attempt
     */
    private void handleFailedPassword(UserDto user, String ipAddress) {
        saveFailedAttempt(user.getEmail(), ipAddress, "INVALID_PASSWORD");
    }
}