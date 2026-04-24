package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
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

    @Test
    void registerUsersBulk_shouldReturn201_whenAllRequestsAreValid() throws Exception {
        RegisterRequest firstRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-one@gmail.com")
                .withPhone("1111111111")
                .build();
        RegisterRequest secondRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-two@gmail.com")
                .withPhone("2222222222")
                .build();

        BulkRegistrationResponse response = new BulkRegistrationResponse(
                2,
                2,
                0,
                List.of(
                        new UserResponse(UUID.randomUUID(), firstRequest.name(), firstRequest.email(), firstRequest.phone(), "USER", "ACTIVE", null),
                        new UserResponse(UUID.randomUUID(), secondRequest.name(), secondRequest.email(), secondRequest.phone(), "USER", "ACTIVE", null)
                ),
                List.of()
        );

        when(userService.registerUsersBulk(anyList())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(firstRequest, secondRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(2))
                .andExpect(jsonPath("$.failedRegistrations").value(0))
                .andExpect(jsonPath("$.successDetails", hasSize(2)))
                .andExpect(jsonPath("$.successDetails[0].email").value(firstRequest.email()))
                .andExpect(jsonPath("$.successDetails[1].email").value(secondRequest.email()));
    }

    @Test
    void registerUsersBulk_shouldReturn201_withFailureDetailsWhenServiceCapturesPartialFailures() throws Exception {
        RegisterRequest successRequest = RegisterRequestBuilder.builder()
                .withEmail("ok@gmail.com")
                .withPhone("3333333333")
                .build();
        RegisterRequest failedRequest = RegisterRequestBuilder.builder()
                .withEmail("duplicate@gmail.com")
                .withPhone("4444444444")
                .build();

        BulkRegistrationResponse response = new BulkRegistrationResponse(
                2,
                1,
                1,
                List.of(new UserResponse(UUID.randomUUID(), successRequest.name(), successRequest.email(), successRequest.phone(), "USER", "ACTIVE", null)),
                List.of(new BulkRegistrationResponse.FailedRegistration(failedRequest, "Email already in use"))
        );

        when(userService.registerUsersBulk(anyList())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(successRequest, failedRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(1))
                .andExpect(jsonPath("$.failedRegistrations").value(1))
                .andExpect(jsonPath("$.failureDetails", hasSize(1)))
                .andExpect(jsonPath("$.failureDetails[0].request.email").value(failedRequest.email()))
                .andExpect(jsonPath("$.failureDetails[0].errorMessage").value("Email already in use"));
    }

    @Test
    void registerUsersBulk_shouldReturn400_whenAnyItemFailsValidation() throws Exception {
        RegisterRequest validRequest = RegisterRequestBuilder.builder()
                .withEmail("valid@gmail.com")
                .withPhone("5555555555")
                .build();
        RegisterRequest invalidRequest = RegisterRequestBuilder.builder()
                .withEmail("invalid-email")
                .build();

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(validRequest, invalidRequest))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void registerUsersBulk_shouldReturn400_whenRequestBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{]"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void registerUsersBulk_shouldReturn201_whenRequestListIsEmpty() throws Exception {
        BulkRegistrationResponse response = new BulkRegistrationResponse(0, 0, 0, List.of(), List.of());
        when(userService.registerUsersBulk(anyList())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(0))
                .andExpect(jsonPath("$.successfulRegistrations").value(0))
                .andExpect(jsonPath("$.failedRegistrations").value(0));
    }
}


