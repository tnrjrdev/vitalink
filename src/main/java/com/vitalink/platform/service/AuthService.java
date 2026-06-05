package com.vitalink.platform.service;

import com.vitalink.platform.dto.auth.AuthResponse;
import com.vitalink.platform.dto.auth.LoginRequest;
import com.vitalink.platform.dto.auth.RefreshTokenRequest;
import com.vitalink.platform.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}
