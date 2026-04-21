package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.exception.GlobalExceptionHandler;
import com.eande.store.user_service.exception.ResourceAlreadyExistsException;
import com.eande.store.user_service.service.UserService;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import com.eande.store.user_service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class UserControllerUintTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;


    // ===============================
    // ✅ SUCCESS CASE
    // ===============================
    @Test
    void registerUser_shouldReturn201_whenValidRequest() throws Exception {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .build();
        UserResponse response = new UserResponse(
                TestDataFactory.userResponse().id(),
                request.name(),
                request.email(),
                request.phone(),
                TestDataFactory.userResponse().role(),
                TestDataFactory.userResponse().status(),
                TestDataFactory.userResponse().addresses()
        );
        when(userService.registerUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(response.email()))
                .andExpect(jsonPath("$.name").value(response.name()));
    }

    // ===============================
    // ❌ INVALID EMAIL
    // ===============================
    @Test
    void registerUser_shouldReturn400_whenEmailIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz.com",
                "Strong@123",
                "+919999999999"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    // ===============================
    // ❌ WEAK PASSWORD
    // ===============================
    @Test
    void registerUser_shouldReturn400_whenPasswordIsWeak() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "12345678",
                "+919999999999"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("password"));
    }

    // ===============================
    // ❌ INVALID PHONE
    // ===============================
    @Test
    void registerUser_shouldReturn400_whenPhoneIsInvalid() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "12345*6"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("phone"));
    }

    // ===============================
    // ❌ EMAIL ALREADY EXISTS
    // ===============================
    @Test
    void registerUser_shouldReturn409_whenEmailAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        when(userService.registerUser(any()))
                .thenThrow(new ResourceAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ===============================
    // ❌ PHONE ALREADY EXISTS
    // ===============================
    @Test
    void registerUser_shouldReturn409_whenPhoneAlreadyExists() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        when(userService.registerUser(any()))
                .thenThrow(new ResourceAlreadyExistsException("Phone already exists"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Phone already exists"));
    }

    // ===============================
    // ❌ MISSING REQUIRED FIELD
    // ===============================
    @Test
    void registerUser_shouldReturn400_whenNameIsBlank() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("name"));
    }

    // ===============================
    // ❌ MULTIPLE FIELD ERRORS
    // ===============================
    @Test
    void registerUser_shouldReturnMultipleValidationErrors() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "",
                "bad-email",
                "123",
                "123"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void registerUser_shouldReturn500_whenUnexpectedError() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        when(userService.registerUser(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void registerUser_shouldReturn500_whenMalformedJson() throws Exception {

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void registerUser_shouldReturn400_whenEmptyJson() throws Exception {

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
    @Test
    void registerUser_shouldHandleNullResponse() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        when(userService.registerUser(any())).thenReturn(null);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("")); // or adjust based on your controller
    }
    @Test
    void registerUser_shouldReturn500_whenContentTypeMissing() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "Fair",
                "fairoz@gmail.com",
                "Strong@123",
                "+919999999999"
        );

        mockMvc.perform(post("/api/v1/users/register")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}


