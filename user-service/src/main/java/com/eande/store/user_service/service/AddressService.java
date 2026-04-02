package com.eande.store.user_service.service;

import com.eande.store.user_service.dto.request.AddressRequest;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.entity.Address;

public interface AddressService {
    AddressResponse createAddress(AddressRequest addressRequest);
}
