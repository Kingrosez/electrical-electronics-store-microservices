package com.eande.store.user_service.integration.user;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class GetUserByIdIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // ✅ SUCCESS
    // ===============================
    @Test
    void shouldReturnUser_whenUserExists() throws Exception {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withEmail("test@gmail.com")
                .withPhone("9000000001")
                .build();

        String response = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String userId = objectMapper.readTree(response).get("id").asText();

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId));
    }

    // ===============================
    // ❌ NOT FOUND
    // ===============================
    @Test
    void shouldReturn404_whenUserNotFound() throws Exception {

        mockMvc.perform(get("/api/v1/users/{id}",
                        UUID.randomUUID()))
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
