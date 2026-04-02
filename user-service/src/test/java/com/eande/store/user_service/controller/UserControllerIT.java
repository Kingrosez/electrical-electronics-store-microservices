package com.eande.store.user_service.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_success() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(request.email()));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().build();

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailIsNotVaild() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withEmail("fairoz.com").build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }
    @Test
    void reigsterUser_shouldThrowExceptionforWeekPassword() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withPassword("12345678").build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void registerUser_shouldThrowException_whenPhoneAlreadyExists() throws  Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().build();
        RegisterRequest request1 = RegisterRequestBuilder.builder().withEmail("fairoz@gmail.com").build();

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone number already in use"));
    }

    @Test
    void registerUser_shouldThrowException_whenPhoneIsNotValid() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("123456").build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    @Test
    void registerUser_nullPhone_shouldWork() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withEmail("fairoza123@gmail.com").withPhone(null).build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());


    }
}
