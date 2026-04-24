package com.eande.store.user_service.controller.user;

import com.eande.store.user_service.controller.UserController;
import com.eande.store.user_service.exception.ResourceNotFoundException;
import com.eande.store.user_service.service.UserService;
import com.eande.store.user_service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;

    // ===============================
    // ✅ SUCCESS
    // ===============================
    @Test
    void shouldReturnUser_whenValidId() throws Exception {

        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId))
                .thenReturn(TestDataFactory.userResponse());

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    // ===============================
    // ❌ NOT FOUND
    // ===============================
    @Test
    void shouldReturn404_whenUserNotFound() throws Exception {

        UUID userId = UUID.randomUUID();

        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    // ===============================
    // ❌ INVALID UUID
    // ===============================
    @Test
    void shouldReturn400_whenInvalidUUID() throws Exception {

        mockMvc.perform(get("/api/v1/users/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }
}
