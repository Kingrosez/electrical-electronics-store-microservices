package com.eande.store.user_service.util;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import com.eande.store.user_service.enums.Role;
import com.eande.store.user_service.enums.Status;

import java.util.UUID;

public class TestDataFactory {


    public static User userEntity(){
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setEmail("johndoe@gmail.com");
        user.setPhone("1234567890");
        user.setRole(Role.USER);
        user.setStatus(Status.ACTIVE);
        return user;
    }

    public static UserResponse userResponse(){
        return new UserResponse(
                UUID.randomUUID(),
                "John Doe",
                "johndoe@gmail.com",
                "1234567890",
                null,
                null,
                null
                );
    }
}
