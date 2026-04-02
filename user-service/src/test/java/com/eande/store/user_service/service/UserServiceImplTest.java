package com.eande.store.user_service.service;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.enums.Status;
import com.eande.store.user_service.exception.EmailAlreadyExistsException;
import com.eande.store.user_service.exception.PhoneNumberAlreadyExistsException;
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
    void registerUser_shouldThrowException_whenEmailIsAlreadyExists() {
    RegisterRequest request= RegisterRequestBuilder.builder().withEmail("duplicate@gmail.com").build();
    when(userRepository.existsByEmail(request.email())).thenReturn(true);
        EmailAlreadyExistsException emailAlreadyExistsException = assertThrows(EmailAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
        assertEquals("Email already in use", emailAlreadyExistsException.getMessage());

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository,never()).save(any());


    }

    @Test
    void registerUser_shouldThrowException_whenPhoneIsAlreadyExists(){
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("9999999999").build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(true);
        PhoneNumberAlreadyExistsException phoneNumberAlreadyExistsException = assertThrows(PhoneNumberAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
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
        assertThrows(EmailAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));
        verify(userRepository,never()).existsByPhone(any());
        verify(userMapper,never()).toEntity(any());

    }
    @Test
    void registerUser_shouldStopExecution_whenPhoneExist(){
        RegisterRequest request = RegisterRequestBuilder.builder().withPhone("9999999999").build();
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThrows(PhoneNumberAlreadyExistsException.class, () -> userServiceImpl.registerUser(request));

        verify(userMapper,never()).toEntity(any());


    }
}
