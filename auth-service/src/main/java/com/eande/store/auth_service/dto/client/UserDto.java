package com.eande.store.auth_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;
    private String status;
    private boolean deleted;
    private boolean emailVerified;

    public boolean isEnabled() {
        return "ACTIVE".equals(status) && !deleted;
    }

    public boolean isAccountNonLocked() {
        return !"BLOCKED".equals(status);
    }

    public String getRoleWithPrefix() {
        return "ROLE_" + role;
    }
}