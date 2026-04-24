package com.eande.store.auth_service.exception;

import lombok.Getter;

@Getter
public abstract class BaseCustomException extends RuntimeException {

    private final String errorCode;
    private final int statusCode;
    private final String userMessage;

    public BaseCustomException(String message, String errorCode, int statusCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }

    public BaseCustomException(String message, String errorCode, int statusCode, String userMessage, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.statusCode = statusCode;
        this.userMessage = userMessage;
    }
}