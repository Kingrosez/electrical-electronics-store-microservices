package com.eande.store.user_service.dto.response;

public record FieldError(
        String field,
        String message
) {
}
