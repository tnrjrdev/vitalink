package com.vitalink.platform.dto.user;

import com.vitalink.platform.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private final UUID id;
    private final String email;
    private final String fullName;
    private final UserStatus status;
    private final Set<String> roles;
    private final Instant createdAt;
}
