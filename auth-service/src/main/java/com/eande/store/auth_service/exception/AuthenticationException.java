package com.eande.store.auth_service.exception;

public class AuthenticationException extends BaseCustomException {

    public AuthenticationException(String message, String errorCode, int statusCode) {
        super(message, errorCode, statusCode, message);
    }

    public AuthenticationException(String message, String errorCode, int statusCode, String userMessage) {
        super(message, errorCode, statusCode, userMessage);
    }

    // Factory methods
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                401,
                "The email or password you entered is incorrect"
        );
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException(
                "Account is locked due to too many failed attempts",
                "ACCOUNT_LOCKED",
                423,
                "Your account has been temporarily locked. Please try again later"
        );
    }

    public static AuthenticationException accountInactive() {
        return new AuthenticationException(
                "Account is not active",
                "ACCOUNT_INACTIVE",
                403,
                "Please verify your email address to activate your account"
        );
    }

    public static AuthenticationException accountDeleted() {
        return new AuthenticationException(
                "Account has been deleted",
                "ACCOUNT_DELETED",
                403,
                "This account has been deleted. Contact support for assistance"
        );
    }

    public static AuthenticationException userNotFound() {
        return new AuthenticationException(
                "User not found",
                "USER_NOT_FOUND",
                404,
                "No account found with this email address"
        );
    }
}