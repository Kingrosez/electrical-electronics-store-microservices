package com.eande.store.user_service.dto.response;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String street,
        String city,
        String district,
        String state,
        String postalCode,
        String country,
        String phone
) {
}
