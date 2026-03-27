package com.eande.store.user_service.mapper;

import com.eande.store.user_service.dto.request.RegisterRequest;
import com.eande.store.user_service.dto.request.UpdateUserRequest;
import com.eande.store.user_service.dto.response.UserResponse;
import com.eande.store.user_service.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.context.annotation.Bean;

import java.util.List;


@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", constant = "USER")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "addresses", ignore = true)
    User toEntity(RegisterRequest request);

    List<User> toEntityList(List<RegisterRequest> requests);

    UserResponse toResponse(User user);
    List<UserResponse> toResponseList(List<User> users);
    @BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UpdateUserRequest request, @MappingTarget User user);
}
