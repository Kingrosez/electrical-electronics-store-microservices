package com.eande.store.user_service.service;
import com.eande.store.user_service.dto.request.*;
import com.eande.store.user_service.dto.response.AddressResponse;
import com.eande.store.user_service.dto.response.AuthResponse;
import com.eande.store.user_service.dto.response.BulkRegistrationResponse;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.enums.Role;
import com.eande.store.user_service.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
public interface UserService {

        // ========================
        // 🔐 AUTH / USER CREATION
        // ========================

        UserResponse registerUser(RegisterRequest request);

        BulkRegistrationResponse registerUsersBulk(List<RegisterRequest> requests);

        AuthResponse login(LoginRequest request);

        AuthResponse refreshToken(RefreshTokenRequest request);

        // ========================
        // 👤 USER RETRIEVAL
        // ========================

        UserResponse getUserById(UUID userId);

        List<UserResponse> getAllUsers(); // optional (avoid in large scale)

        Page<UserResponse> getUsers(Pageable pageable);

        List<UserResponse> getUsersByIds(List<UUID> userIds);

        UserResponse getUserByEmail(String email);

        List<UserResponse> getUsersByRole(Role role);

        List<UserResponse> getUsersByStatus(Status status);

        Page<UserResponse> filterUsers(Role role, Status status, Pageable pageable);

        List<UserResponse> searchUsers(String keyword);

        // ========================
        // ✏️ USER UPDATE
        // ========================

        UserResponse updateUser(UUID userId, UpdateUserRequest request);

        void deactivateUser(UUID userId);

        void deactivateUsersBulk(List<UUID> userIds);

        // ========================
        // 🔒 SECURITY
        // ========================

        void changePassword(UUID userId, ChangePasswordRequest request);

        UserResponse getCurrentUser();

        boolean existsByEmail(String email);

        // ========================
        // 🏠 ADDRESS MANAGEMENT
        // ========================

        AddressResponse addAddress(UUID userId, AddressRequest request);

        List<AddressResponse> getUserAddresses(UUID userId);

        AddressResponse updateAddress(UUID addressId, UpdateAddressRequest request);

        void deleteAddress(UUID addressId);

        AddressResponse setDefaultAddress(UUID userId, UUID addressId);

        // ========================
        // ⚙️ ADMIN OPERATIONS
        // ========================

        UserResponse updateUserRole(UUID userId, UpdateRoleRequest request);

        UserResponse updateUserStatus(UUID userId, UpdateStatusRequest request);


}

