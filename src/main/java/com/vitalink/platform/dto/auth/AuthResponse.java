package com.vitalink.platform.dto.auth;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    @Builder.Default
    private final String tokenType = "Bearer";
    private final long expiresIn;
    private final UUID userId;
    private final String email;
    private final Set<String> roles;
}
