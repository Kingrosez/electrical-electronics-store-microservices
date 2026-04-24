package com.eande.store.auth_service.exception;

public class ResourceNotFoundException extends BaseCustomException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue),
                "RESOURCE_NOT_FOUND",
                404,
                String.format("%s not found", resourceName)
        );
    }

    public ResourceNotFoundException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
    }

    // Factory methods
    public static ResourceNotFoundException userNotFound(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }

    public static ResourceNotFoundException userNotFoundById(String userId) {
        return new ResourceNotFoundException("User", "id", userId);
    }

    public static ResourceNotFoundException refreshTokenNotFound() {
        return new ResourceNotFoundException(
                "Refresh token not found",
                "REFRESH_TOKEN_NOT_FOUND",
                404
        );
    }

    public static ResourceNotFoundException resourceNotFound(String resourceName) {
        return new ResourceNotFoundException(
                String.format("%s not found", resourceName),
                "RESOURCE_NOT_FOUND",
                404
        );
    }
}