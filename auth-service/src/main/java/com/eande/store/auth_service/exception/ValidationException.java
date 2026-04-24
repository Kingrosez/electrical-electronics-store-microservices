package com.eande.store.auth_service.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends BaseCustomException {

    private final Map<String, String> fieldErrors;

    public ValidationException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
        this.fieldErrors = null;
    }

    public ValidationException(String message, String errorCode, int statusCode, Map<String, String> fieldErrors) {
        super(message, errorCode, statusCode, message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(String message, String errorCode, int statusCode, String userMessage) {
        super(message, errorCode, statusCode, userMessage);
        this.fieldErrors = null;
    }

    // Factory methods
    public static ValidationException emailRequired() {
        return new ValidationException(
                "Email is required",
                "EMAIL_REQUIRED",
                400,
                "Please provide your email address"
        );
    }

    public static ValidationException invalidEmailFormat() {
        return new ValidationException(
                "Invalid email format",
                "INVALID_EMAIL",
                400,
                "Please enter a valid email address"
        );
    }

    public static ValidationException passwordRequired() {
        return new ValidationException(
                "Password is required",
                "PASSWORD_REQUIRED",
                400,
                "Please provide your password"
        );
    }

    public static ValidationException invalidPasswordLength() {
        return new ValidationException(
                "Password must be between 6 and 100 characters",
                "INVALID_PASSWORD_LENGTH",
                400,
                "Password must be between 6 and 100 characters"
        );
    }

    public static ValidationException refreshTokenRequired() {
        return new ValidationException(
                "Refresh token is required",
                "REFRESH_TOKEN_REQUIRED",
                400,
                "Please provide a refresh token"
        );
    }

    public static ValidationException withFieldErrors(Map<String, String> fieldErrors) {
        return new ValidationException(
                "Validation failed",
                "VALIDATION_FAILED",
                400,
                fieldErrors
        );
    }
}