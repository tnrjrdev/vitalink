package com.vitalink.platform.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    @NotBlank
    private String secret;

    @Min(60_000)
    private long accessTokenExpirationMs;

    @Min(60_000)
    private long refreshTokenExpirationMs;

    @NotBlank
    private String issuer;
}
