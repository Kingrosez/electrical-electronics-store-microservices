package com.eande.store.auth_service.exception;

import com.eande.store.auth_service.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    // ============================================================
    // HELPER METHODS TO BUILD RESPONSES
    // ============================================================

    /**
     * Build error response from BaseCustomException
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(
            BaseCustomException ex,
            HttpServletRequest request) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(ex.getUserMessage())
                .error(ex.getErrorCode())
                .statusCode(ex.getStatusCode())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Build error response with custom parameters
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(
            String message,
            String errorCode,
            int statusCode,
            HttpServletRequest request) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .statusCode(statusCode)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Build error response with data field (for validation errors)
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponseWithData(
            T data,
            String message,
            String errorCode,
            int statusCode,
            HttpServletRequest request) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .data(data)
                .message(message)
                .error(errorCode)
                .statusCode(statusCode)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(statusCode).body(response);
    }

    /**
     * Build error response with Retry-After header (for rate limiting)
     */
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponseWithRetryAfter(
            String message,
            String errorCode,
            int statusCode,
            long retryAfterSeconds,
            HttpServletRequest request) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .statusCode(statusCode)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(statusCode)
                .header("Retry-After", String.valueOf(retryAfterSeconds))
                .body(response);
    }

    /**
     * Log warning with consistent format
     */
    private void logWarning(String errorCode, String message, String path) {
        log.warn("{}: {} - Path: {}", errorCode, message, path);
    }

    /**
     * Log error with consistent format
     */
    private void logError(String errorCode, String message, String path, Exception ex) {
        log.error("{}: {} - Path: {} - Error: {}", errorCode, message, path, ex.getMessage(), ex);
    }

    // ============================================================
    // CUSTOM EXCEPTION HANDLERS
    // ============================================================

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleTokenException(
            TokenException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            RateLimitException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        return buildErrorResponseWithRetryAfter(
                ex.getUserMessage(),
                ex.getErrorCode(),
                ex.getStatusCode(),
                ex.getRetryAfterSeconds(),
                request
        );
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailableException(
            ServiceUnavailableException ex,
            HttpServletRequest request) {

        logError(ex.getErrorCode(), ex.getMessage(), request.getRequestURI(), ex);
        return buildErrorResponse(ex, request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {

        logWarning(ex.getErrorCode(), ex.getMessage(), request.getRequestURI());

        if (ex.getFieldErrors() != null) {
            return buildErrorResponseWithData(
                    ex.getFieldErrors(),
                    "Validation failed",
                    ex.getErrorCode(),
                    ex.getStatusCode(),
                    request
            );
        } else {
            return buildErrorResponse(
                    ex.getUserMessage(),
                    ex.getErrorCode(),
                    ex.getStatusCode(),
                    request
            );
        }
    }

    // ============================================================
    // SPRING SECURITY EXCEPTION HANDLERS
    // ============================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
            HttpServletRequest request) {

        logWarning("INVALID_CREDENTIALS", "Bad credentials", request.getRequestURI());

        return buildErrorResponse(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED.value(),
                request
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFound(
            HttpServletRequest request) {

        logWarning("INVALID_CREDENTIALS", "Username not found", request.getRequestURI());

        return buildErrorResponse(
                "Invalid email or password",
                "INVALID_CREDENTIALS",
                HttpStatus.UNAUTHORIZED.value(),
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleSpringAccessDenied(
            HttpServletRequest request) {

        logWarning("ACCESS_DENIED", "Access denied", request.getRequestURI());

        return buildErrorResponse(
                "You don't have permission to access this resource",
                "ACCESS_DENIED",
                HttpStatus.FORBIDDEN.value(),
                request
        );
    }

    // ============================================================
    // JWT EXCEPTION HANDLERS
    // ============================================================

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(
            HttpServletRequest request) {

        logWarning("TOKEN_EXPIRED", "JWT token expired", request.getRequestURI());

        return buildErrorResponse(
                "Token has expired. Please refresh or login again.",
                "TOKEN_EXPIRED",
                HttpStatus.UNAUTHORIZED.value(),
                request
        );
    }

    @ExceptionHandler(io.jsonwebtoken.MalformedJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJwt(
            HttpServletRequest request) {

        logWarning("INVALID_TOKEN", "Malformed JWT token", request.getRequestURI());

        return buildErrorResponse(
                "Invalid token format",
                "INVALID_TOKEN",
                HttpStatus.UNAUTHORIZED.value(),
                request
        );
    }

    @ExceptionHandler(io.jsonwebtoken.security.SignatureException.class)
    public ResponseEntity<ApiResponse<Void>> handleSignatureException(
            HttpServletRequest request) {

        logWarning("INVALID_TOKEN_SIGNATURE", "Invalid JWT signature", request.getRequestURI());

        return buildErrorResponse(
                "Invalid token signature",
                "INVALID_TOKEN_SIGNATURE",
                HttpStatus.UNAUTHORIZED.value(),
                request
        );
    }

    // ============================================================
    // VALIDATION EXCEPTION HANDLERS
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logWarning("VALIDATION_ERROR", "Validation failed: " + errors, request.getRequestURI());

        return buildErrorResponseWithData(
                errors,
                "Validation failed",
                "VALIDATION_ERROR",
                HttpStatus.BAD_REQUEST.value(),
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String errors = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        logWarning("CONSTRAINT_VIOLATION", "Constraint violation: " + errors, request.getRequestURI());

        return buildErrorResponseWithData(
                errors,
                "Constraint violation: " + errors,
                "CONSTRAINT_VIOLATION",
                HttpStatus.BAD_REQUEST.value(),
                request
        );
    }

    // ============================================================
    // HTTP & REQUEST EXCEPTION HANDLERS
    // ============================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Invalid parameter format: " + ex.getName();
        logWarning("INVALID_PARAMETER", message, request.getRequestURI());

        return buildErrorResponse(
                message,
                "INVALID_PARAMETER",
                HttpStatus.BAD_REQUEST.value(),
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpServletRequest request) {

        logWarning("MALFORMED_JSON", "Malformed JSON request body", request.getRequestURI());

        return buildErrorResponse(
                "Malformed JSON request body",
                "MALFORMED_JSON",
                HttpStatus.BAD_REQUEST.value(),
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            HttpServletRequest request) {

        logWarning("NOT_FOUND", "Resource not found", request.getRequestURI());

        return buildErrorResponse(
                "Resource not found",
                "NOT_FOUND",
                HttpStatus.NOT_FOUND.value(),
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        logWarning("ILLEGAL_ARGUMENT", ex.getMessage(), request.getRequestURI());

        return buildErrorResponse(
                ex.getMessage(),
                "ILLEGAL_ARGUMENT",
                HttpStatus.BAD_REQUEST.value(),
                request
        );
    }

    // ============================================================
    // GENERIC EXCEPTION HANDLERS (CATCH-ALL)
    // ============================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        logError("INTERNAL_SERVER_ERROR", "Unexpected runtime error", request.getRequestURI(), ex);

        return buildErrorResponse(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        logError("INTERNAL_SERVER_ERROR", "Unexpected error", request.getRequestURI(), ex);

        return buildErrorResponse(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request
        );
    }
}