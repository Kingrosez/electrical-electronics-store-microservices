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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegisterUsersBulkIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // ✅ FULL SUCCESS
    // ===============================
    @Test
    void shouldCreateAllUsers_whenValidRequest() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder()
                        .withEmail("a@gmail.com")
                        .withPhone("1111111111")
                        .build(),

                RegisterRequestBuilder.builder()
                        .withEmail("b@gmail.com")
                        .withPhone("2222222222") // 🔥 DIFFERENT PHONE
                        .build()
        );

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.successfulRegistrations").value(2))
                .andExpect(jsonPath("$.failedRegistrations").value(0));
    }

    // ===============================
    // ⚠️ PARTIAL SUCCESS
    // ===============================
    @Test
    void shouldReturnPartialSuccess_whenSomeUsersFail() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder().withEmail("same@gmail.com").build(),
                RegisterRequestBuilder.builder().withEmail("same@gmail.com").build()
        );

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failedRegistrations").value(1));
    }

    // ===============================
    // ❌ DUPLICATE IN DB
    // ===============================
    @Test
    void shouldFail_whenDuplicateEmailExistsInDB() throws Exception {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withEmail("dup@gmail.com")
                .build();

        // First insert
        mockMvc.perform(post("/api/v1/users/bulk-register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(request))));

        // Second insert (duplicate)
        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failedRegistrations").value(1));
    }

    // ===============================
    // ❌ INVALID EMAIL
    // ===============================
    @Test
    void shouldReturn400_whenInvalidEmail() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder().withEmail("bad").build()
        );

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // ❌ EMPTY LIST
    // ===============================
    @Test
    void shouldReturn400_whenEmptyList() throws Exception {

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // ❌ MALFORMED JSON
    // ===============================
    @Test
    void shouldReturn400_whenMalformedJson() throws Exception {

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }
}
