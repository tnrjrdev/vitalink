package com.vitalink.platform.service;

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
import com.vitalink.platform.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;

    @InjectMocks private AuthServiceImpl authService;

    private Role patientRole;

    @BeforeEach
    void setUp() {
        patientRole = Role.builder().name(RoleName.PATIENT).build();
        patientRole.setId(UUID.randomUUID());
    }

    private void stubTokenGeneration() {
        when(tokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("refresh-token");
        when(tokenProvider.getAccessTokenExpiresInSeconds()).thenReturn(900L);
    }

    @Test
    @DisplayName("registra novo paciente e retorna tokens")
    void shouldRegister() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Joao Paciente");
        request.setEmail("joao@medico.com");
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.PATIENT));

        when(userRepository.existsByEmailIgnoreCase("joao@medico.com")).thenReturn(false);
        when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
        when(passwordEncoder.encode("Senha123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        stubTokenGeneration();

        AuthResponse response = authService.register(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getEmail()).isEqualTo("joao@medico.com");
        assertThat(response.getRoles()).contains(RoleName.PATIENT);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("falha ao registrar e-mail ja existente")
    void shouldFailDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("dup@medico.com");
        request.setFullName("Dup");
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.PATIENT));

        when(userRepository.existsByEmailIgnoreCase("dup@medico.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("falha ao registrar com perfil privilegiado nao permitido")
    void shouldFailDisallowedRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("hacker@medico.com");
        request.setFullName("Hacker");
        request.setPassword("Senha123");
        request.setRoles(Set.of(RoleName.ADMIN));

        when(userRepository.existsByEmailIgnoreCase("hacker@medico.com")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao permitido");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("autentica usuario valido e emite tokens")
    void shouldLogin() {
        LoginRequest request = new LoginRequest();
        request.setEmail("joao@medico.com");
        request.setPassword("Senha123");

        User user = User.builder()
                .email("joao@medico.com").password("hashed").fullName("Joao")
                .status(UserStatus.ACTIVE).roles(Set.of(patientRole)).build();
        user.setId(UUID.randomUUID());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(UserPrincipal.from(user), null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        stubTokenGeneration();

        AuthResponse response = authService.login(request);

        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("renova access token com refresh valido")
    void shouldRefresh() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh");

        User user = User.builder()
                .email("joao@medico.com").password("hashed").fullName("Joao")
                .status(UserStatus.ACTIVE).roles(Set.of(patientRole)).build();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        when(tokenProvider.validate("valid-refresh")).thenReturn(true);
        when(tokenProvider.isRefreshToken("valid-refresh")).thenReturn(true);
        when(tokenProvider.getUserId("valid-refresh")).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        stubTokenGeneration();

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("falha ao renovar com token invalido")
    void shouldFailRefreshInvalid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("bad");

        when(tokenProvider.validate("bad")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class);
        verify(userRepository, never()).findById(any());
    }
}
