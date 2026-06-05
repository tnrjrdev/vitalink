package com.vitalink.platform.service.impl;

import com.vitalink.platform.common.exception.BusinessException;
import com.vitalink.platform.common.exception.DuplicateResourceException;
import com.vitalink.platform.dto.auth.AuthResponse;
import com.vitalink.platform.dto.auth.LoginRequest;
import com.vitalink.platform.dto.auth.RefreshTokenRequest;
import com.vitalink.platform.dto.auth.RegisterRequest;
import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.entity.enums.UserStatus;
import com.vitalink.platform.repository.RoleRepository;
import com.vitalink.platform.repository.UserRepository;
import com.vitalink.platform.security.JwtTokenProvider;
import com.vitalink.platform.security.UserPrincipal;
import com.vitalink.platform.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final Set<String> SELF_ASSIGNABLE_ROLES =
            Set.of(RoleName.PATIENT, RoleName.PROFESSIONAL);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Usuario", "e-mail", request.getEmail());
        }

        Set<Role> roles = resolveSelfAssignableRoles(request.getRoles());

        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        user = userRepository.save(user);
        log.info("Novo usuario registrado: id={}", user.getId());

        return buildAuthResponse(UserPrincipal.from(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        log.info("Login bem-sucedido: id={}", principal.getId());
        return buildAuthResponse(principal);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!tokenProvider.validate(token) || !tokenProvider.isRefreshToken(token)) {
            throw new BusinessException("Refresh token invalido ou expirado");
        }

        UUID userId = tokenProvider.getUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuario do token nao existe mais"));

        if (!user.isActive()) {
            throw new BusinessException("Conta inativa ou bloqueada");
        }

        log.info("Access token renovado: id={}", userId);
        return buildAuthResponse(UserPrincipal.from(user));
    }

    private Set<Role> resolveSelfAssignableRoles(Set<String> requestedRoles) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : requestedRoles) {
            if (!SELF_ASSIGNABLE_ROLES.contains(roleName)) {
                throw new BusinessException(
                        "Perfil nao permitido no auto-registro: " + roleName);
            }
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new BusinessException("Perfil inexistente: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    private AuthResponse buildAuthResponse(UserPrincipal principal) {
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);
        Set<String> roleNames = principal.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenProvider.getAccessTokenExpiresInSeconds())
                .userId(principal.getId())
                .email(principal.getEmail())
                .roles(roleNames)
                .build();
    }
}
