package com.vitalink.platform.security;

import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.entity.enums.UserStatus;
import com.vitalink.platform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService")
class CustomUserDetailsServiceTest {
    @Mock private UserRepository userRepository;

    @InjectMocks private CustomUserDetailsService service;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = Role.builder().name(RoleName.ADMIN).build();
        role.setId(UUID.randomUUID());
        user = User.builder()
                .email("admin@medico.com").password("hash").fullName("Admin")
                .status(UserStatus.ACTIVE).roles(Set.of(role)).build();
        user.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("loadUserByUsername resolve por e-mail e expoe authorities")
    void shouldLoadByUsername() {
        when(userRepository.findByEmailIgnoreCase("admin@medico.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("admin@medico.com");

        assertThat(details.getUsername()).isEqualTo("admin@medico.com");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getAuthorities())
                .anyMatch(a -> a.getAuthority().equals(RoleName.ADMIN));
    }

    @Test
    @DisplayName("loadUserByUsername lanca quando inexistente")
    void shouldThrowWhenUsernameMissing() {
        when(userRepository.findByEmailIgnoreCase("x@medico.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("x@medico.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("loadUserById resolve por id")
    void shouldLoadById() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserById(user.getId());

        assertThat(details.getUsername()).isEqualTo("admin@medico.com");
    }

    @Test
    @DisplayName("loadUserById lanca quando inexistente")
    void shouldThrowWhenIdMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserById(id))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("conta bloqueada nao fica habilitada")
    void shouldReflectBlockedStatus() {
        user.setStatus(UserStatus.BLOCKED);
        when(userRepository.findByEmailIgnoreCase("admin@medico.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("admin@medico.com");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }
}
