package com.eande.store.user_service.controller;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;

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
    void registerUser_shouldThrowException_whenEmailIsNotValid() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withEmail("fairoz.com").build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].message").value("Invalid email format"));
    }
    @Test
    void registerUser_shouldThrowExceptionWeekPassword() throws Exception {
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
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("12345*6").build();
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

    @Test
    void registerUser_shouldFail_whenEmailIsBlank() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withEmail("").build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].message").value("Email is required"));
    }

    @Test
    void registerUser_shouldFail_whenNameIsBlank() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withName("").build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'name')].message").value("Name is required"));

    }

    @Test
    void registerUser_shouldFail_whenPhoneNumberIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("").build();
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'phone')].message").value("Invalid phone number format"));

    }


    @Test
    void registerUser_shouldHandleDatabaseConstrainsViolation() throws Exception {
        // First request (valid)
        RegisterRequest request = RegisterRequestBuilder.builder()
                .withEmail("test@gmail.com")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second request with SAME email
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // 409 is best practice
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @Test
    void registerUser_shouldReturnMultipleFieldErrors() throws Exception {
        RegisterRequest request = RegisterRequestBuilder.builder()
                .withName("")
                .withEmail("invalid-email")
                .withPassword("")
                .withPhone("123@")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))

                .andExpect(jsonPath("$.fieldErrors[?(@.field=='name')].message")
                        .value(hasItem("Name is required")))

                .andExpect(jsonPath("$.fieldErrors[?(@.field=='email')].message")
                        .value(hasItem("Invalid email format")))

                .andExpect(jsonPath("$.fieldErrors[?(@.field=='password')].message")
                        .value(hasItem("Password is required")))

                .andExpect(jsonPath("$.fieldErrors[?(@.field=='phone')].message")
                        .value(hasItem("Invalid phone number format")));
    }

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void registerUser_shouldFail_forInvalidPasswords(String password, String expectedMessage) throws Exception {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withPassword(password)
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='password')].message")
                        .value(hasItem(expectedMessage)));
    }

    static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("", "Password is required"),
                Arguments.of("abc", "Password must be at least 8 characters")
        );
    }
}
