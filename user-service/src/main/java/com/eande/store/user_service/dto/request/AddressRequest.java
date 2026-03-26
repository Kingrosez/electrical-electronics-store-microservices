package com.eande.store.user_service.dto.request;

import jakarta.persistence.Column;

public record AddressRequest(
        String street,
        String city,
        String state,
        String district,
        String postalCode,
        String country,
        String phone,
        boolean isDefault
) {
}
