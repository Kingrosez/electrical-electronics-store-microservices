package com.eande.store.auth_service.security; // ← from client package
import com.eande.store.auth_service.client.UserServiceClient;
import com.eande.store.auth_service.dto.client.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDto userDto;

        if (username.contains("@")) {
            userDto = userServiceClient.findByEmail(username).orElseThrow(() ->
                    new UsernameNotFoundException("User not found: " + username));
        } else {
            try {
                UUID userId = UUID.fromString(username);
                userDto = userServiceClient.findById(userId).orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + userId));
            } catch (IllegalArgumentException e) {
                throw new UsernameNotFoundException("Invalid username format");
            }
        }

        if (userDto.isDeleted() || !userDto.isEnabled()) {
            throw new UsernameNotFoundException("Account not active");
        }

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(userDto.getRoleWithPrefix());

        return User.builder()
                .username(userDto.getId().toString())
                .password(userDto.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountLocked(!userDto.isAccountNonLocked())
                .disabled(!userDto.isEnabled())
                .build();
    }
}
