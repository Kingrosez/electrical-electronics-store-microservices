package com.eande.store.auth_service.service;

import com.eande.store.auth_service.client.UserServiceClient;
import com.eande.store.auth_service.dto.client.UserDto;
import com.eande.store.auth_service.dto.client.UserValidationResponse;
import com.eande.store.auth_service.dto.request.LoginRequest;
import com.eande.store.auth_service.dto.request.RefreshTokenRequest;
import com.eande.store.auth_service.dto.response.TokenResponse;
import com.eande.store.auth_service.entity.LoginAttempt;
import com.eande.store.auth_service.entity.RefreshToken;
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
                    recordFailedAttempt(request.getEmail(), ipAddress, "USER_NOT_FOUND");
                    return new RuntimeException("Invalid email or password");
                });

        // Check if account is locked
        if ("BLOCKED".equals(user.getStatus())) {
            log.warn("Login failed - account blocked for user: {}", user.getEmail());
            throw new RuntimeException("Account is blocked. Contact support.");
        }

        // Check if account is inactive
        if (!user.isEnabled()) {
            log.warn("Login failed - account inactive for user: {}", user.getEmail());
            throw new RuntimeException("Account is not active. Please verify your email.");
        }

        // Verify password using User Service validation
        UserValidationResponse validationResponse = userServiceClient.validateCredentials(
                request.getEmail(), request.getPassword()
        );

        if (!validationResponse.isValid()) {
            log.warn("Login failed - invalid password for user: {}", user.getId());
            handleFailedPassword(user, ipAddress);
            recordFailedAttempt(request.getEmail(), ipAddress, "INVALID_PASSWORD");
            throw new RuntimeException("Invalid email or password");
        }

        // Success - reset failed attempts
        resetFailedLoginAttempts(user.getEmail());
        recordSuccessfulLogin(user, ipAddress, userAgent);

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
            throw new RuntimeException("Invalid refresh token");
        }

        // Check token type
        String tokenType = jwtService.extractTokenType(refreshTokenString);
        if (!"refresh".equals(tokenType)) {
            log.warn("Token type mismatch - expected refresh, got: {}", tokenType);
            throw new RuntimeException("Invalid token type");
        }

        String userIdStr = jwtService.extractUserId(refreshTokenString);
        UUID userId = UUID.fromString(userIdStr);

        // Find stored refresh token
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenAndRevokedFalse(refreshTokenString)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found or revoked for user: {}", userId);
                    return new RuntimeException("Refresh token has been revoked");
                });

        // Check expiry
        if (storedToken.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expired for user: {}", userId);
            refreshTokenRepository.revokeByToken(refreshTokenString);
            throw new RuntimeException("Refresh token has expired. Please login again.");
        }

        // Fetch user from User Service
        UserDto user = userServiceClient.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found during refresh: {}", userId);
                    return new RuntimeException("User not found");
                });

        // Check if user is still active
        if (!user.isEnabled() || user.isDeleted()) {
            log.warn("User is no longer active during refresh: {}", userId);
            throw new RuntimeException("Account is no longer active");
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
     * Generate and store refresh token
     */
    private String generateAndStoreRefreshToken(UserDto user, String ipAddress, String userAgent) {
        String refreshTokenString = jwtService.generateRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .userId(user.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiryDate(Instant.now().plusSeconds(jwtService.getRefreshTokenExpirationSeconds()))
                .revoked(false)
                .build();

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
            log.warn("Account locked due to too many failed attempts: {}", email);
            throw new RuntimeException("Too many failed attempts. Please try again later.");
        }
    }

    /**
     * Record failed login attempt
     */
    private void recordFailedAttempt(String email, String ipAddress, String reason) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(email)
                .ipAddress(ipAddress)
                .success(false)
                .failureReason(reason)
                .build();

        loginAttemptRepository.save(attempt);
    }

    /**
     * Handle failed password attempt
     */
    private void handleFailedPassword(UserDto user, String ipAddress) {
        recordFailedAttempt(user.getEmail(), ipAddress, "INVALID_PASSWORD");
        // Could implement account locking after too many attempts here
    }

    /**
     * Reset failed login attempts
     */
    private void resetFailedLoginAttempts(String email) {
        log.debug("Failed login attempts reset for: {}", email);
    }

    /**
     * Record successful login
     */
    private void recordSuccessfulLogin(UserDto user, String ipAddress, String userAgent) {
        LoginAttempt attempt = LoginAttempt.builder()
                .email(user.getEmail())
                .ipAddress(ipAddress)
                .success(true)
                .failureReason(null)
                .build();

        loginAttemptRepository.save(attempt);
    }
}