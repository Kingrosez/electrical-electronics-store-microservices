package com.eande.store.user_service.controller.bulkRegister;

import com.eande.store.user_service.controller.UserController;
import com.eande.store.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import com.eande.store.user_service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegisterUsersBulkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // ===============================
    // ✅ FULL SUCCESS
    // ===============================
    @Test
    void bulkRegister_shouldReturn201_whenAllSuccess() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder().build()
        );

        BulkRegistrationResponse response = new BulkRegistrationResponse(
                1,
                1,
                0,
                List.of(TestDataFactory.userResponse()),
                List.of()
        );

        when(userService.registerUsersBulk(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalProcessed").value(1))
                .andExpect(jsonPath("$.successfulRegistrations").value(1))
                .andExpect(jsonPath("$.failedRegistrations").value(0))
                .andExpect(jsonPath("$.successDetails").isArray())
                .andExpect(jsonPath("$.failureDetails").isEmpty());
    }

    // ===============================
    // ⚠️ PARTIAL SUCCESS
    // ===============================
    @Test
    void bulkRegister_shouldReturn200_whenPartialSuccess() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder().build()
        );

        BulkRegistrationResponse response = new BulkRegistrationResponse(
                2,
                1,
                1,
                List.of(TestDataFactory.userResponse()),
                List.of(new BulkRegistrationResponse.FailedRegistration(
                        RegisterRequestBuilder.builder().build(),
                        "Email already exists"
                ))
        );

        when(userService.registerUsersBulk(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(2))
                .andExpect(jsonPath("$.successfulRegistrations").value(1))
                .andExpect(jsonPath("$.failedRegistrations").value(1))
                .andExpect(jsonPath("$.failureDetails[0].errorMessage")
                        .value("Email already exists"));
    }

    // ===============================
    // ❌ FULL FAILURE
    // ===============================
    @Test
    void bulkRegister_shouldReturn200_whenAllFailed() throws Exception {

        BulkRegistrationResponse response = new BulkRegistrationResponse(
                2,
                0,
                2,
                List.of(),
                List.of(
                        new BulkRegistrationResponse.FailedRegistration(
                                RegisterRequestBuilder.builder().build(),
                                "Invalid email"
                        ),
                        new BulkRegistrationResponse.FailedRegistration(
                                RegisterRequestBuilder.builder().build(),
                                "Duplicate phone"
                        )
                )
        );

        when(userService.registerUsersBulk(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulRegistrations").value(0))
                .andExpect(jsonPath("$.failedRegistrations").value(2))
                .andExpect(jsonPath("$.failureDetails").isArray());
    }

    // ===============================
    // ❌ VALIDATION ERROR
    // ===============================
    @Test
    void bulkRegister_shouldReturn400_whenInvalidEmail() throws Exception {

        List<RegisterRequest> request = List.of(
                RegisterRequestBuilder.builder().withEmail("bad").build()
        );

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("email"));
    }

    // ===============================
    // ❌ MALFORMED JSON
    // ===============================
    @Test
    void bulkRegister_shouldReturn400_whenMalformedJson() throws Exception {

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // ❌ EMPTY LIST
    // ===============================
    @Test
    void bulkRegister_shouldReturn400_whenEmptyList() throws Exception {

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // ❌ SERVICE EXCEPTION (500)
    // ===============================
    @Test
    void bulkRegister_shouldReturn500_whenUnexpectedError() throws Exception {

        when(userService.registerUsersBulk(any()))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/api/v1/users/bulk-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                List.of(RegisterRequestBuilder.builder().build())
                        )))
                .andExpect(status().isInternalServerError());
    }

    // ===============================
    // ❌ CONTENT TYPE MISSING
    // ===============================

}
