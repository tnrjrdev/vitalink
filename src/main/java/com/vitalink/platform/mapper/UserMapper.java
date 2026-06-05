package com.vitalink.platform.mapper;

import com.vitalink.platform.dto.user.UserResponse;
import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
