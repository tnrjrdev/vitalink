package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.dto.user.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse getCurrentUser();

    UserResponse getById(UUID id);

    PageResponse<UserResponse> list(Pageable pageable);
}
