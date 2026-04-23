package com.eande.store.user_service.service;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.enums.Role;
import com.eande.store.user_service.enums.Status;
import com.eande.store.user_service.exception.ResourceAlreadyExistsException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    void registerUser_success() {
        RegisterRequest request = RegisterRequestBuilder.builder().build();

        UserResponse response = TestDataFactory.userResponse();

        User user = TestDataFactory.userEntity();
        User savedUser = TestDataFactory.userEntity();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn(request.password());
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = userServiceImpl.registerUser(request);

        // ✅ Assertions
        assertNotNull(result);
        assertEquals("johndoe@gmail.com", result.email());

        // ✅ Correct verifications
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByPhone(request.phone());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(user);
    }

    @Test
    void registerUsersBulk_shouldReturnAllSuccessfulRegistrations() {
        RegisterRequest firstRequest = RegisterRequestBuilder.builder().build();
        RegisterRequest secondRequest = RegisterRequestBuilder.builder()
                .withEmail("second@gmail.com")
                .withPhone("9876543210")
                .build();

        User firstUser = TestDataFactory.userEntity();
        User secondUser = TestDataFactory.userEntity();
        User firstSavedUser = TestDataFactory.userEntity();
        User secondSavedUser = TestDataFactory.userEntity();

        UserResponse firstResponse = new UserResponse(
                firstSavedUser.getId(),
                firstRequest.name(),
                firstRequest.email(),
                firstRequest.phone(),
                "USER",
                "ACTIVE",
                null
        );
        UserResponse secondResponse = new UserResponse(
                secondSavedUser.getId(),
                secondRequest.name(),
                secondRequest.email(),
                secondRequest.phone(),
                "USER",
                "ACTIVE",
                null
        );

        when(userRepository.existsByEmail(firstRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(firstRequest.phone())).thenReturn(false);
        when(userRepository.existsByEmail(secondRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(secondRequest.phone())).thenReturn(false);
        when(userMapper.toEntity(firstRequest)).thenReturn(firstUser);
        when(userMapper.toEntity(secondRequest)).thenReturn(secondUser);
        when(passwordEncoder.encode(firstRequest.password())).thenReturn("encoded-first");
        when(passwordEncoder.encode(secondRequest.password())).thenReturn("encoded-second");
        when(userRepository.save(firstUser)).thenReturn(firstSavedUser);
        when(userRepository.save(secondUser)).thenReturn(secondSavedUser);
        when(userMapper.toResponse(firstSavedUser)).thenReturn(firstResponse);
        when(userMapper.toResponse(secondSavedUser)).thenReturn(secondResponse);

        BulkRegistrationResponse result = userServiceImpl.registerUsersBulk(List.of(firstRequest, secondRequest));

        assertNotNull(result);
        assertEquals(2, result.totalProcessed());
        assertEquals(2, result.successfulRegistrations());
        assertEquals(0, result.failedRegistrations());
        assertEquals(2, result.successDetails().size());
        assertTrue(result.failureDetails().isEmpty());
        assertEquals(firstRequest.email(), result.successDetails().getFirst().email());
        assertEquals(secondRequest.email(), result.successDetails().get(1).email());

        verify(userRepository).existsByEmail(firstRequest.email());
        verify(userRepository).existsByPhone(firstRequest.phone());
        verify(userRepository).existsByEmail(secondRequest.email());
        verify(userRepository).existsByPhone(secondRequest.phone());
        verify(passwordEncoder).encode(firstRequest.password());
        verify(passwordEncoder).encode(secondRequest.password());
        verify(userRepository).save(firstUser);
        verify(userRepository).save(secondUser);
    }

    @Test
    void registerUsersBulk_shouldContinueProcessingWhenOneRequestFails() {
        RegisterRequest firstRequest = RegisterRequestBuilder.builder()
                .withEmail("first@gmail.com")
                .withPhone("1111111111")
                .build();
        RegisterRequest duplicateEmailRequest = RegisterRequestBuilder.builder()
                .withEmail("duplicate@gmail.com")
                .withPhone("2222222222")
                .build();
        RegisterRequest thirdRequest = RegisterRequestBuilder.builder()
                .withEmail("third@gmail.com")
                .withPhone("3333333333")
                .build();

        User firstUser = new User();
        User thirdUser = new User();
        User firstSavedUser = TestDataFactory.userEntity();
        firstSavedUser.setId(UUID.randomUUID());
        firstSavedUser.setEmail(firstRequest.email());
        firstSavedUser.setPhone(firstRequest.phone());
        User thirdSavedUser = TestDataFactory.userEntity();
        thirdSavedUser.setId(UUID.randomUUID());
        thirdSavedUser.setEmail(thirdRequest.email());
        thirdSavedUser.setPhone(thirdRequest.phone());

        UserResponse firstResponse = new UserResponse(
                firstSavedUser.getId(),
                firstRequest.name(),
                firstRequest.email(),
                firstRequest.phone(),
                "USER",
                "ACTIVE",
                null
        );
        UserResponse thirdResponse = new UserResponse(
                thirdSavedUser.getId(),
                thirdRequest.name(),
                thirdRequest.email(),
                thirdRequest.phone(),
                "USER",
                "ACTIVE",
                null
        );

        when(userRepository.existsByEmail(firstRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(firstRequest.phone())).thenReturn(false);
        when(userRepository.existsByEmail(duplicateEmailRequest.email())).thenReturn(true);
        when(userRepository.existsByEmail(thirdRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(thirdRequest.phone())).thenReturn(false);
        when(userMapper.toEntity(firstRequest)).thenReturn(firstUser);
        when(userMapper.toEntity(thirdRequest)).thenReturn(thirdUser);
        when(passwordEncoder.encode(firstRequest.password())).thenReturn("encoded-first");
        when(passwordEncoder.encode(thirdRequest.password())).thenReturn("encoded-third");
        when(userRepository.save(firstUser)).thenReturn(firstSavedUser);
        when(userRepository.save(thirdUser)).thenReturn(thirdSavedUser);
        when(userMapper.toResponse(firstSavedUser)).thenReturn(firstResponse);
        when(userMapper.toResponse(thirdSavedUser)).thenReturn(thirdResponse);

        BulkRegistrationResponse result = userServiceImpl.registerUsersBulk(
                List.of(firstRequest, duplicateEmailRequest, thirdRequest)
        );

        assertNotNull(result);
        assertEquals(3, result.totalProcessed());
        assertEquals(2, result.successfulRegistrations());
        assertEquals(1, result.failedRegistrations());
        assertEquals(List.of(firstRequest.email(), thirdRequest.email()),
                result.successDetails().stream().map(UserResponse::email).toList());
        assertEquals(duplicateEmailRequest.email(), result.failureDetails().getFirst().request().email());
        assertEquals("Email already in use", result.failureDetails().getFirst().errorMessage());

        verify(userRepository).existsByEmail(duplicateEmailRequest.email());
        verify(userRepository, never()).existsByPhone(duplicateEmailRequest.phone());
        verify(userMapper, never()).toEntity(duplicateEmailRequest);
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void registerUsersBulk_shouldCapturePhoneConflictAsFailure() {
        RegisterRequest validRequest = RegisterRequestBuilder.builder()
                .withEmail("valid@gmail.com")
                .withPhone("4444444444")
                .build();
        RegisterRequest duplicatePhoneRequest = RegisterRequestBuilder.builder()
                .withEmail("other@gmail.com")
                .withPhone("5555555555")
                .build();

        User user = new User();
        User savedUser = TestDataFactory.userEntity();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(validRequest.email());
        savedUser.setPhone(validRequest.phone());
        UserResponse successResponse = new UserResponse(
                savedUser.getId(),
                validRequest.name(),
                validRequest.email(),
                validRequest.phone(),
                "USER",
                "ACTIVE",
                null
        );

        when(userRepository.existsByEmail(validRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(validRequest.phone())).thenReturn(false);
        when(userRepository.existsByEmail(duplicatePhoneRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(duplicatePhoneRequest.phone())).thenReturn(true);
        when(userMapper.toEntity(validRequest)).thenReturn(user);
        when(passwordEncoder.encode(validRequest.password())).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(successResponse);

        BulkRegistrationResponse result = userServiceImpl.registerUsersBulk(
                List.of(validRequest, duplicatePhoneRequest)
        );

        assertEquals(2, result.totalProcessed());
        assertEquals(1, result.successfulRegistrations());
        assertEquals(1, result.failedRegistrations());
        assertEquals(duplicatePhoneRequest.email(), result.failureDetails().getFirst().request().email());
        assertEquals("Phone number already in use", result.failureDetails().getFirst().errorMessage());

        verify(userRepository).existsByPhone(duplicatePhoneRequest.phone());
        verify(userRepository, never()).save(argThat(saved -> duplicatePhoneRequest.email().equals(saved.getEmail())));
    }

    @Test
    void registerUsersBulk_shouldReturnEmptyResponse_whenRequestListIsEmpty() {
        BulkRegistrationResponse result = userServiceImpl.registerUsersBulk(List.of());

        assertNotNull(result);
        assertEquals(0, result.totalProcessed());
        assertEquals(0, result.successfulRegistrations());
        assertEquals(0, result.failedRegistrations());
        assertTrue(result.successDetails().isEmpty());
        assertTrue(result.failureDetails().isEmpty());

        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void registerUsersBulk_shouldReturnEmptyResponse_whenRequestListIsNull() {
        BulkRegistrationResponse result = userServiceImpl.registerUsersBulk(null);

        assertNotNull(result);
        assertEquals(0, result.totalProcessed());
        assertEquals(0, result.successfulRegistrations());
        assertEquals(0, result.failedRegistrations());
        assertTrue(result.successDetails().isEmpty());
        assertTrue(result.failureDetails().isEmpty());

        verifyNoInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void registerUser_shouldThrowException_whenEmailIsAlreadyExists() {
    RegisterRequest request= RegisterRequestBuilder.builder().withEmail("duplicate@gmail.com").build();
    when(userRepository.existsByEmail(request.email())).thenReturn(true);
        ResourceAlreadyExistsException emailAlreadyExistsException = assertThrows(ResourceAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
        assertEquals("Email already in use", emailAlreadyExistsException.getMessage());

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository,never()).save(any());


    }

    @Test
    void registerUser_shouldThrowException_whenPhoneIsAlreadyExists(){
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("9999999999").build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(true);
        ResourceAlreadyExistsException phoneNumberAlreadyExistsException = assertThrows(ResourceAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
        assertEquals("Phone number already in use", phoneNumberAlreadyExistsException.getMessage());
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).existsByPhone(request.phone());
        verify(userRepository,never()).save(any());
    }
    @Test
    void registerUser_shouldWork_whenPhoneIsNull(){
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone(null).build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(TestDataFactory.userEntity());
        when(passwordEncoder.encode(request.password())).thenReturn(request.password());
        when(userRepository.save(any())).thenReturn(TestDataFactory.userEntity());
        when(userMapper.toResponse(any())).thenReturn(TestDataFactory.userResponse());

        userServiceImpl.registerUser(request);

        verify(userRepository,never()).existsByPhone(any());

    }
    @Test
    void registerUser_shouldHashThePassword(){
        RegisterRequest request = RegisterRequestBuilder.builder().build();
        UserResponse response = TestDataFactory.userResponse();
        User user = TestDataFactory.userEntity();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);
        userServiceImpl.registerUser(request);

        assertEquals("hashedPassword",user.getPassword());

    }
    @Test
    void registerUser_shouldSetStatusAndRole(){
        RegisterRequest request = RegisterRequestBuilder.builder().build();
        UserResponse response = TestDataFactory.userResponse();
        User user = TestDataFactory.userEntity();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn(request.password());
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);
        userServiceImpl.registerUser(request);

        assertEquals(Status.ACTIVE,user.getStatus());
        assertNotNull(user.getRole());


    }
    @Test
    void registerUser_shouldStopExecution_whenEmailExist(){
        RegisterRequest request = RegisterRequestBuilder.builder().build();

        when(userRepository.existsByEmail(request.email())).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
        verify(userRepository,never()).existsByPhone(any());
        verify(userMapper,never()).toEntity(any());

    }
    @Test
    void registerUser_shouldStopExecution_whenPhoneExist(){
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("9999999999").build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));

        verify(userMapper,never()).toEntity(any());


    }
    @Test
    void registerUser_shouldWork_whenPhoneIsBlank(){
        RegisterRequest request = RegisterRequestBuilder.builder()
                .withPhone(" ").build();

        User user = new User();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(TestDataFactory.userResponse());

        UserResponse response = userServiceImpl.registerUser(request);

        assertNotNull(response);
        verify(userRepository,never()).existsByPhone(any());
        verify(userRepository).save(any());
    }

    @Test
    void registerUser_shouldStoreHashedPassword_notRawPassword(){
        RegisterRequest request = RegisterRequestBuilder.builder().build();
        User user = new User();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(TestDataFactory.userResponse());
        userServiceImpl.registerUser(request);

        assertEquals("hashedPassword",user.getPassword());
        assertEquals("hashedPassword",user.getPassword());
    }

    @Test
    void registerUsersBulk_shouldSetDefaultRoleStatusAndEncodedPasswordForEverySuccessfulUser() {
        RegisterRequest firstRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-one@gmail.com")
                .withPhone("6666666666")
                .build();
        RegisterRequest secondRequest = RegisterRequestBuilder.builder()
                .withEmail("bulk-two@gmail.com")
                .withPhone("7777777777")
                .build();

        User firstUser = new User();
        User secondUser = new User();
        User firstSavedUser = new User();
        firstSavedUser.setId(UUID.randomUUID());
        User secondSavedUser = new User();
        secondSavedUser.setId(UUID.randomUUID());

        when(userRepository.existsByEmail(firstRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(firstRequest.phone())).thenReturn(false);
        when(userRepository.existsByEmail(secondRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(secondRequest.phone())).thenReturn(false);
        when(userMapper.toEntity(firstRequest)).thenReturn(firstUser);
        when(userMapper.toEntity(secondRequest)).thenReturn(secondUser);
        when(passwordEncoder.encode(firstRequest.password())).thenReturn("encoded-one");
        when(passwordEncoder.encode(secondRequest.password())).thenReturn("encoded-two");
        when(userRepository.save(firstUser)).thenReturn(firstSavedUser);
        when(userRepository.save(secondUser)).thenReturn(secondSavedUser);
        when(userMapper.toResponse(firstSavedUser)).thenReturn(new UserResponse(firstSavedUser.getId(), firstRequest.name(), firstRequest.email(), firstRequest.phone(), "USER", "ACTIVE", null));
        when(userMapper.toResponse(secondSavedUser)).thenReturn(new UserResponse(secondSavedUser.getId(), secondRequest.name(), secondRequest.email(), secondRequest.phone(), "USER", "ACTIVE", null));

        userServiceImpl.registerUsersBulk(List.of(firstRequest, secondRequest));

        assertEquals(Role.USER, firstUser.getRole());
        assertEquals(Status.ACTIVE, firstUser.getStatus());
        assertEquals("encoded-one", firstUser.getPassword());
        assertEquals(Role.USER, secondUser.getRole());
        assertEquals(Status.ACTIVE, secondUser.getStatus());
        assertEquals("encoded-two", secondUser.getPassword());
    }
}
