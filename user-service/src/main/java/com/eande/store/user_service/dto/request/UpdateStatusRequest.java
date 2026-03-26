package com.eande.store.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateStatusRequest(
        @NotBlank
        String status
) {
}
