package com.eande.store.user_service.util.user;


import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RegisterRequestBuilderTest {

    @Test
    void shouldBuildValidRegisterRequest() {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withName("John")
                .withEmail("john@gmail.com")
                .withPassword("Password@123")
                .withPhone("9999999999")
                .build();

        assertEquals("John", request.name());
        assertEquals("john@gmail.com", request.email());
        assertEquals("Password@123", request.password());
        assertEquals("9999999999", request.phone());
    }

    @Test
    void shouldAllowNullPhone() {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withPhone(null)
                .build();

        assertNull(request.phone());
    }
}