package com.eande.store.user_service.dto.response;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String phone,
        String role,
        String status,
        List<AddressResponse> addresses
) {
}
