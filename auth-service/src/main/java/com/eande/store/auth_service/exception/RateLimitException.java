package com.eande.store.auth_service.exception;

public class RateLimitException extends BaseCustomException {

    private final long retryAfterSeconds;

    public RateLimitException(String message, long retryAfterSeconds) {
        super(message, "RATE_LIMIT_EXCEEDED", 429, message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitException(String message, String errorCode, int statusCode, String userMessage, long retryAfterSeconds) {
        super(message, errorCode, statusCode, userMessage);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    // Factory methods
    public static RateLimitException tooManyLoginAttempts(long retryAfterSeconds) {
        return new RateLimitException(
                "Too many login attempts. Please try again later.",
                "TOO_MANY_LOGIN_ATTEMPTS",
                429,
                "Too many failed attempts. Please try again after " + retryAfterSeconds + " seconds",
                retryAfterSeconds
        );
    }

    public static RateLimitException rateLimitExceeded(long retryAfterSeconds) {
        return new RateLimitException(
                "Rate limit exceeded",
                "RATE_LIMIT_EXCEEDED",
                429,
                "Too many requests. Please try again later.",
                retryAfterSeconds
        );
    }
}