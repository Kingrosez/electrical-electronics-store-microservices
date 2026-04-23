package com.eande.store.user_service.util;

import com.eande.store.user_service.dto.response.UserResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestDataFactoryTest {

    @Test
    void shouldCreateUserResponse() {

        UserResponse response = TestDataFactory.userResponse();

        assertNotNull(response);
        assertNotNull(response.id());
        assertNotNull(response.email());
        assertEquals("ACTIVE", response.status());
    }
}
