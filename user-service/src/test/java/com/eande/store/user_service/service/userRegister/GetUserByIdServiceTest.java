package com.eande.store.user_service.service.userRegister;

import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.exception.BadRequestException;
import com.eande.store.user_service.exception.ResourceNotFoundException;
import com.eande.store.user_service.mapper.UserMapper;
import com.eande.store.user_service.repository.UserRepository;
import com.eande.store.user_service.service.impl.UserServiceImpl;
import com.eande.store.user_service.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserByIdServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ===============================
    // ✅ SUCCESS
    // ===============================
    @Test
    void shouldReturnUser_whenUserExists() {

        UUID userId = UUID.randomUUID();
        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(TestDataFactory.userResponse());

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
    }

    // ===============================
    // ❌ NOT FOUND
    // ===============================
    @Test
    void shouldThrowException_whenUserNotFound() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(userId));
    }

    // ===============================
    // ❌ NULL ID
    // ===============================
    @Test
    void shouldThrowException_whenUserIdIsNull() {

        assertThrows(BadRequestException.class,
                () -> userService.getUserById(null));
    }
}
