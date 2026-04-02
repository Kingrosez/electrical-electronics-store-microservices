package com.eande.store.user_service.service.impl;

import com.eande.store.user_service.dto.request.AddressRequest;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.entity.Address;
import com.eande.store.user_service.mapper.AddressMapper;
import com.eande.store.user_service.repository.AddressRepository;
import com.eande.store.user_service.service.AddressService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;


    @Override
    public AddressResponse createAddress(AddressRequest addressRequest) {
        log.info("Creating address for user address");
        Address address = addressMapper.toEntity(addressRequest);
        Address saved = addressRepository.save(address);
        log.info("Saved address for user address with id: {}", saved.getId());
        return addressMapper.toResponse(saved);
    }
}
