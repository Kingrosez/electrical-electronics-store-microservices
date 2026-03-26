package com.eande.store.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateRoleRequest(
        @NotBlank
        String role
) {
}
