package com.eande.store.user_service.integration.user;

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

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;

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

    @Test
    void registerUsersBulk_shouldRegisterAllUsersSuccessfully() throws Exception {
        RegisterRequest firstRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-success-one@gmail.com")
                .withPhone("1111111111")
                .build();
        RegisterRequest secondRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-success-two@gmail.com")
                .withPhone("2222222222")
                .build();

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(firstRequest, secondRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(2))
                .andExpect(jsonPath("$.failedRegistrations").value(0))
                .andExpect(jsonPath("$.successDetails", hasSize(2)))
                .andExpect(jsonPath("$.successDetails[0].email").value(firstRequest.email()))
                .andExpect(jsonPath("$.successDetails[1].email").value(secondRequest.email()))
                .andExpect(jsonPath("$.failureDetails", hasSize(0)));
    }

    @Test
    void registerUsersBulk_shouldReturnMixedResult_whenSomeUsersAlreadyExist() throws Exception {
        RegisterRequest existingRequest = RegisterRequestBuilder.builder()
                .withEmail("existing-bulk@gmail.com")
                .withPhone("3333333333")
                .build();
        RegisterRequest duplicateEmailRequest = RegisterRequestBuilder.builder()
                .withEmail(existingRequest.email())
                .withPhone("4444444444")
                .build();
        RegisterRequest validRequest = RegisterRequestBuilder.builder()
                .withEmail("fresh-bulk@gmail.com")
                .withPhone("5555555555")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(duplicateEmailRequest, validRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(1))
                .andExpect(jsonPath("$.failedRegistrations").value(1))
                .andExpect(jsonPath("$.successDetails", hasSize(1)))
                .andExpect(jsonPath("$.successDetails[0].email").value(validRequest.email()))
                .andExpect(jsonPath("$.failureDetails", hasSize(1)))
                .andExpect(jsonPath("$.failureDetails[0].request.email").value(duplicateEmailRequest.email()))
                .andExpect(jsonPath("$.failureDetails[0].errorMessage").value("Email already in use"));
    }

    @Test
    void registerUsersBulk_shouldReturnMixedResult_whenDuplicatePhoneExists() throws Exception {
        RegisterRequest existingRequest = RegisterRequestBuilder.builder()
                .withEmail("existing-phone@gmail.com")
                .withPhone("6666666666")
                .build();
        RegisterRequest duplicatePhoneRequest = RegisterRequestBuilder.builder()
                .withEmail("duplicate-phone@gmail.com")
                .withPhone(existingRequest.phone())
                .build();
        RegisterRequest validRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-phone-valid@gmail.com")
                .withPhone("7777777777")
                .build();

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(duplicatePhoneRequest, validRequest))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(1))
                .andExpect(jsonPath("$.failedRegistrations").value(1))
                .andExpect(jsonPath("$.failureDetails[0].request.email").value(duplicatePhoneRequest.email()))
                .andExpect(jsonPath("$.failureDetails[0].errorMessage").value("Phone number already in use"));
    }

    @Test
    void registerUsersBulk_shouldReturn400_whenAnyUserIsInvalid() throws Exception {
        RegisterRequest validRequest = RegisterRequestBuilder.builder()
                .withEmail("valid-bulk@gmail.com")
                .withPhone("8888888888")
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
    void registerUsersBulk_shouldReturnEmptySummary_whenRequestListIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(0))
                .andExpect(jsonPath("$.successfulRegistrations").value(0))
                .andExpect(jsonPath("$.failedRegistrations").value(0))
                .andExpect(jsonPath("$.successDetails", hasSize(0)))
                .andExpect(jsonPath("$.failureDetails", hasSize(0)));
    }
}
