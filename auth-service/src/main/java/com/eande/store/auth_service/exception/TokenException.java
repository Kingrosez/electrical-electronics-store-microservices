package com.eande.store.auth_service.exception;

public class TokenException extends BaseCustomException {

    public TokenException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
    }

    public TokenException(String message, String errorCode, int statusCode, String userMessage) {
        super(message, errorCode, statusCode, userMessage);
    }

    // Factory methods
    public static TokenException tokenExpired() {
        return new TokenException(
                "Access token has expired",
                "TOKEN_EXPIRED",
                401,
                "Your session has expired. Please login again"
        );
    }

    public static TokenException refreshTokenExpired() {
        return new TokenException(
                "Refresh token has expired",
                "REFRESH_TOKEN_EXPIRED",
                401,
                "Your session has expired. Please login again"
        );
    }

    public static TokenException invalidToken() {
        return new TokenException(
                "Invalid token",
                "INVALID_TOKEN",
                401,
                "Invalid authentication token"
        );
    }

    public static TokenException malformedToken() {
        return new TokenException(
                "Malformed token",
                "MALFORMED_TOKEN",
                401,
                "Invalid token format"
        );
    }

    public static TokenException tokenSignatureInvalid() {
        return new TokenException(
                "Token signature is invalid",
                "INVALID_SIGNATURE",
                401,
                "Invalid token signature"
        );
    }

    public static TokenException refreshTokenRevoked() {
        return new TokenException(
                "Refresh token has been revoked",
                "REFRESH_TOKEN_REVOKED",
                401,
                "This session has been terminated. Please login again"
        );
    }

    public static TokenException missingToken() {
        return new TokenException(
                "No token provided",
                "MISSING_TOKEN",
                401,
                "Authentication token is required"
        );
    }

    public static TokenException wrongTokenType() {
        return new TokenException(
                "Wrong token type",
                "WRONG_TOKEN_TYPE",
                401,
                "Invalid token type for this operation"
        );
    }
}