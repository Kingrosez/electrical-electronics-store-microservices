package com.eande.store.user_service.dto.request;

public record UpdateAddressRequest(
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
