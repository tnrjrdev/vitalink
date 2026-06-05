package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.user.UserResponse;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.mapper.UserMapper;
import com.vitalink.platform.repository.UserRepository;
import com.vitalink.platform.security.SecurityUtils;
import com.vitalink.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser() {
        UUID userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum usuario autenticado no contexto"));
        return getById(userId);
    }

    @Override
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> list(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }
}
