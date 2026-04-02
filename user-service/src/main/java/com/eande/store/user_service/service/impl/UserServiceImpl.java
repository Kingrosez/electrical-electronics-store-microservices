package com.eande.store.user_service.service.impl;

import com.eande.store.user_service.dto.request.*;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.dto.response.AuthResponse;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.enums.Role;
import com.eande.store.user_service.enums.Status;
import com.eande.store.user_service.exception.EmailAlreadyExistsException;
import com.eande.store.user_service.exception.PhoneNumberAlreadyExistsException;
import com.eande.store.user_service.mapper.UserMapper;
import com.eande.store.user_service.repository.UserRepository;
import com.eande.store.user_service.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(RegisterRequest request) {
        return registerUserInternally(request);
    }

    @Override
    public List<UserResponse> registerUsersBulk(List<RegisterRequest> requests) {
        return List.of();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return null;
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        return null;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return List.of();
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return null;
    }

    @Override
    public List<UserResponse> getUsersByIds(List<UUID> userIds) {
        return List.of();
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        return null;
    }

    @Override
    public List<UserResponse> getUsersByRole(Role role) {
        return List.of();
    }

    @Override
    public List<UserResponse> getUsersByStatus(Status status) {
        return List.of();
    }

    @Override
    public Page<UserResponse> filterUsers(Role role, Status status, Pageable pageable) {
        return null;
    }

    @Override
    public List<UserResponse> searchUsers(String keyword) {
        return List.of();
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
        return null;
    }

    @Override
    public void deactivateUser(UUID userId) {

    }

    @Override
    public void deactivateUsersBulk(List<UUID> userIds) {

    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest request) {

    }

    @Override
    public UserResponse getCurrentUser() {
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        return null;
    }

    @Override
    public List<AddressResponse> getUserAddresses(UUID userId) {
        return List.of();
    }

    @Override
    public AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request) {
        return null;
    }

    @Override
    public void deleteAddress(UUID addressId) {

    }

    @Override
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        return null;
    }

    @Override
    public UserResponse updateUserRole(UUID userId, UpdateRoleRequest request) {
        return null;
    }

    @Override
    public UserResponse updateUserStatus(UUID userId, UpdateStatusRequest request) {
        return null;
    }

    UserResponse registerUserInternally(RegisterRequest request) {
        log.info("Registering user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email {} already exists", request.email());
            throw new EmailAlreadyExistsException("Email already in use");
        }
        if ( request.phone() != null && !request.phone().isBlank() && userRepository.existsByPhone(request.phone())) {
            log.warn("Registration failed: Phone number {} already exists", request.phone());
            throw new PhoneNumberAlreadyExistsException("Phone number already in use");
        }
        User user = userMapper.toEntity(request);
        user.setStatus(Status.ACTIVE);
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        log.info("Successfully registered user with email: {}", request.email());
        return userMapper.toResponse(saved);


    }
}
