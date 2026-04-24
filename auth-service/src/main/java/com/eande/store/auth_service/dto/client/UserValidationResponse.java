package com.eande.store.auth_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserValidationResponse {
    private boolean valid;
    private UUID userId;
    private String email;
    private String role;
    private String status;
    private boolean accountNonLocked;
    private boolean enabled;
    private String errorMessage;
}