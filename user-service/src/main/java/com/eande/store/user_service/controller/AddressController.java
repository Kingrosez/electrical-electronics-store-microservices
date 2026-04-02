package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.AddressRequest;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.entity.Address;
import com.eande.store.user_service.service.AddressService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/addresses")
@AllArgsConstructor
@Slf4j
public class AddressController {
    private final AddressService addressService;
    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(@Valid @RequestBody AddressRequest addressRequest) {
        log.info("Received request to create address for user address: {}", addressRequest);
        AddressResponse addressResponse = addressService.createAddress(addressRequest);
        log.info("Created address for user address: {}", addressRequest);
        return ResponseEntity.ok(addressResponse);
    }
}
