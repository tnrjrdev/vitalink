package com.vitalink.platform.service;

import com.vitalink.platform.common.dto.PageResponse;
import com.vitalink.platform.common.exception.ResourceNotFoundException;
import com.vitalink.platform.dto.user.UserResponse;
import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.entity.enums.UserStatus;
import com.vitalink.platform.mapper.UserMapper;
import com.vitalink.platform.repository.UserRepository;
import com.vitalink.platform.security.UserPrincipal;
import com.vitalink.platform.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {
    @Mock private UserRepository userRepository;

    private UserServiceImpl service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository, new UserMapper());
        Role role = Role.builder().name(RoleName.PATIENT).build();
        role.setId(UUID.randomUUID());
        user = User.builder()
                .email("user@medico.com").password("hash").fullName("Usuario Teste")
                .status(UserStatus.ACTIVE).roles(Set.of(role)).build();
        user.setId(UUID.randomUUID());
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getById retorna o usuario mapeado")
    void shouldGetById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = service.getById(user.getId());

        assertThat(response.getEmail()).isEqualTo("user@medico.com");
        assertThat(response.getRoles()).contains(RoleName.PATIENT);
    }

    @Test
    @DisplayName("getById lanca 404 quando nao existe")
    void shouldThrowWhenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getCurrentUser usa o id do contexto de seguranca")
    void shouldGetCurrentUser() {
        UserPrincipal principal = UserPrincipal.from(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponse response = service.getCurrentUser();

        assertThat(response.getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("getCurrentUser falha sem contexto autenticado")
    void shouldFailWithoutContext() {
        assertThatThrownBy(() -> service.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("list pagina os usuarios")
    void shouldList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserResponse> response = service.list(pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getEmail()).isEqualTo("user@medico.com");
    }
}
