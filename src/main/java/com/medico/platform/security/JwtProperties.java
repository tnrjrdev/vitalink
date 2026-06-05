package com.medico.platform.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * propriedades de configuracao do JWT, mapeadas de {@code app.jwt.*}.
 * Tipadas e validadas na inicializacao (fail-fast).
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** chave secreta (Base64) para assinatura HMAC-SHA512. Minimo 64 bytes. */
    @NotBlank
    private String secret;

    /** validade do access token em milissegundos. */
    @Min(60_000)
    private long accessTokenExpirationMs;

    /** validade do refresh token em milissegundos. */
    @Min(60_000)
    private long refreshTokenExpirationMs;

    /** emissor (claim "iss"). */
    @NotBlank
    private String issuer;
}
