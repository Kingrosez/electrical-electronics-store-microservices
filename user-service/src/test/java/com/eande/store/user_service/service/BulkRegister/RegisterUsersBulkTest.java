package com.eande.store.user_service.service.BulkRegister;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.exception.BadRequestException;
import com.eande.store.user_service.mapper.UserMapper;
import com.eande.store.user_service.repository.UserRepository;
import com.eande.store.user_service.service.impl.UserServiceImpl;
import com.eande.store.user_service.util.RegisterRequestBuilder;
import com.eande.store.user_service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUsersBulkTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    // ===============================
    // ✅ FULL SUCCESS
    // ===============================
    @Test
    void shouldReturnAllSuccess_whenAllValid() {

        List<RegisterRequest> requests = List.of(
                RegisterRequestBuilder.builder().withEmail("a@gmail.com").build(),
                RegisterRequestBuilder.builder().withEmail("b@gmail.com").build()
        );

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(new User());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(new User());
        when(userMapper.toResponse(any())).thenReturn(TestDataFactory.userResponse());

        BulkRegistrationResponse response = userService.registerUsersBulk(requests);

        assertEquals(2, response.totalProcessed());
        assertEquals(2, response.successfulRegistrations());
        assertEquals(0, response.failedRegistrations());
        assertEquals(2, response.successDetails().size());
        assertTrue(response.failureDetails().isEmpty());
    }

    // ===============================
    // ⚠️ PARTIAL SUCCESS
    // ===============================
    @Test
    void shouldReturnPartialSuccess_whenSomeUsersFail() {

        RegisterRequest valid = RegisterRequestBuilder.builder()
                .withEmail("valid@gmail.com").build();

        RegisterRequest duplicate = RegisterRequestBuilder.builder()
                .withEmail("dup@gmail.com").build();

        List<RegisterRequest> requests = List.of(valid, duplicate);

        when(userRepository.existsByEmail("valid@gmail.com")).thenReturn(false);
        when(userRepository.existsByEmail("dup@gmail.com")).thenReturn(true);

        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(new User());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(new User());
        when(userMapper.toResponse(any())).thenReturn(TestDataFactory.userResponse());

        BulkRegistrationResponse response = userService.registerUsersBulk(requests);

        assertEquals(2, response.totalProcessed());
        assertEquals(1, response.successfulRegistrations());
        assertEquals(1, response.failedRegistrations());
    }

    // ===============================
    // ❌ FULL FAILURE
    // ===============================
    @Test
    void shouldReturnAllFailures_whenAllInvalid() {

        List<RegisterRequest> requests = List.of(
                RegisterRequestBuilder.builder().withEmail("a@gmail.com").build(),
                RegisterRequestBuilder.builder().withEmail("b@gmail.com").build()
        );

        when(userRepository.existsByEmail(any())).thenReturn(true);

        BulkRegistrationResponse response = userService.registerUsersBulk(requests);

        assertEquals(2, response.totalProcessed());
        assertEquals(0, response.successfulRegistrations());
        assertEquals(2, response.failedRegistrations());
        assertTrue(response.successDetails().isEmpty());
        assertEquals(2, response.failureDetails().size());
    }

    // ===============================
    // ❌ DUPLICATE EMAIL IN REQUEST
    // ===============================
    @Test
    void shouldCaptureFailure_whenDuplicateEmailsInRequest() {

        List<RegisterRequest> requests = List.of(
                RegisterRequestBuilder.builder().withEmail("same@gmail.com").build(),
                RegisterRequestBuilder.builder().withEmail("same@gmail.com").build()
        );

        BulkRegistrationResponse response = userService.registerUsersBulk(requests);

        assertEquals(2, response.totalProcessed());
        assertEquals(2, response.failedRegistrations()); // BOTH fail
    }

    // ===============================
    // ❌ DUPLICATE PHONE IN REQUEST
    // ===============================
    @Test
    void shouldCaptureFailure_whenDuplicatePhonesInRequest() {

        List<RegisterRequest> requests = List.of(
                RegisterRequestBuilder.builder().withPhone("+919999999999").build(),
                RegisterRequestBuilder.builder().withPhone("+919999999999").build()
        );

        BulkRegistrationResponse response = userService.registerUsersBulk(requests);

        assertTrue(response.failedRegistrations() > 0);
    }

    // ===============================
    // 🔐 PASSWORD HASHING
    // ===============================
    @Test
    void shouldHashPasswordsForSuccessfulUsersOnly() {

        RegisterRequest request = RegisterRequestBuilder.builder().build();
        User user = new User();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(user);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(TestDataFactory.userResponse());

        userService.registerUsersBulk(List.of(request));

        assertEquals("hashed", user.getPassword());
    }

    // ===============================
    // ⚠️ NULL INPUT
    // ===============================
    @Test
    void shouldThrowException_whenInputIsNull() {
        assertThrows(BadRequestException.class,
                () -> userService.registerUsersBulk(null));
    }

    // ===============================
    // ⚠️ EMPTY INPUT
    // ===============================
    @Test
    void shouldThrowException_whenInputIsEmpty() {
        assertThrows(BadRequestException.class,
                () -> userService.registerUsersBulk(List.of()));
    }

    // ===============================
    // ⚠️ NULL PHONE
    // ===============================
    @Test
    void shouldHandleNullPhoneValues() {

        RegisterRequest request = RegisterRequestBuilder.builder()
                .withPhone(null)
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userMapper.toEntity(any())).thenReturn(new User());
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenReturn(new User());
        when(userMapper.toResponse(any())).thenReturn(TestDataFactory.userResponse());

        BulkRegistrationResponse response =
                userService.registerUsersBulk(List.of(request));

        assertEquals(1, response.successfulRegistrations());
    }
}