package com.eande.store.user_service.exception;

import com.eande.store.user_service.dto.response.ErrorResponse;
import com.eande.store.user_service.dto.response.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================
    // Common Response Builder
    // =========================================
    private ResponseEntity<ErrorResponse> responseBuilder(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldError> fieldErrors
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors != null ? fieldErrors : List.of()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // =========================================
    // @Valid RequestBody Validation (MOST IMPORTANT)
    // =========================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldError(
                        err.getField(),
                        err.getDefaultMessage()
                ))
                .toList();

        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                request,
                fieldErrors
        );
    }

    // =========================================
    // Method-level validation (Spring Boot 3)
    // =========================================
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {

        List<FieldError> fieldErrors = ex.getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof org.springframework.validation.FieldError fieldError) {
                        return new FieldError(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        );
                    }
                    return new FieldError(
                            "unknown",
                            error.getDefaultMessage()
                    );
                })
                .toList();

        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                request,
                fieldErrors
        );
    }

    // =========================================
    // Malformed JSON
    // =========================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request",
                request,
                List.of()
        );
    }

    // =========================================
    // Illegal Argument (Bad Request)
    // =========================================
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                List.of()
        );
    }

    // =========================================
    // Custom BadRequestException (Optional)
    // =========================================
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request,
                List.of()
        );
    }

    // =========================================
    // Resource Already Exists (409)
    // =========================================
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request,
                List.of()
        );
    }

    // =========================================
    // Generic Exception (Fallback)
    // =========================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                request,
                List.of()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return responseBuilder(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {

        return responseBuilder(
                HttpStatus.BAD_REQUEST,
                "Invalid UUID format",
                request,
                List.of()
        );
    }
}