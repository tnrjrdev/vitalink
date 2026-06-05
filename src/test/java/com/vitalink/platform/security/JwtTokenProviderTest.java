package com.vitalink.platform.security;

import com.vitalink.platform.entity.Role;
import com.vitalink.platform.entity.User;
import com.vitalink.platform.entity.enums.RoleName;
import com.vitalink.platform.entity.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {
    private JwtTokenProvider tokenProvider;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("uma-chave-secreta-de-teste-muito-longa-com-mais-de-64-bytes-para-hmac-sha512!");
        properties.setAccessTokenExpirationMs(900_000);
        properties.setRefreshTokenExpirationMs(604_800_000);
        properties.setIssuer("medico-platform-test");
        tokenProvider = new JwtTokenProvider(properties);

        User user = User.builder()
                .email("user@medico.com")
                .password("hash")
                .fullName("Usuario Teste")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.builder().name(RoleName.PATIENT).build()))
                .build();
        user.setId(UUID.randomUUID());
        principal = UserPrincipal.from(user);
    }

    @Test
    @DisplayName("gera access token valido e recupera o id do usuario")
    void shouldGenerateAndValidateAccessToken() {
        String token = tokenProvider.generateAccessToken(principal);

        assertThat(tokenProvider.validate(token)).isTrue();
        assertThat(tokenProvider.isAccessToken(token)).isTrue();
        assertThat(tokenProvider.isRefreshToken(token)).isFalse();
        assertThat(tokenProvider.getUserId(token)).isEqualTo(principal.getId());
    }

    @Test
    @DisplayName("gera refresh token marcado como refresh")
    void shouldGenerateRefreshToken() {
        String token = tokenProvider.generateRefreshToken(principal);

        assertThat(tokenProvider.validate(token)).isTrue();
        assertThat(tokenProvider.isRefreshToken(token)).isTrue();
        assertThat(tokenProvider.isAccessToken(token)).isFalse();
    }

    @Test
    @DisplayName("rejeita token adulterado")
    void shouldRejectTamperedToken() {
        String token = tokenProvider.generateAccessToken(principal);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(tokenProvider.validate(tampered)).isFalse();
    }

    @Test
    @DisplayName("rejeita token assinado com outra chave")
    void shouldRejectTokenWithDifferentKey() {
        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("outra-chave-completamente-diferente-com-mais-de-64-bytes-para-hmac-sha512-ok!");
        otherProps.setAccessTokenExpirationMs(900_000);
        otherProps.setRefreshTokenExpirationMs(604_800_000);
        otherProps.setIssuer("medico-platform-test");
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        String foreignToken = otherProvider.generateAccessToken(principal);

        assertThat(tokenProvider.validate(foreignToken)).isFalse();
    }

    @Test
    @DisplayName("expira em segundos reflete a configuracao")
    void shouldExposeExpiresInSeconds() {
        assertThat(tokenProvider.getAccessTokenExpiresInSeconds()).isEqualTo(900);
    }
}
