package com.vitalink.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLES = "roles";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = buildKey(properties.getSecret());
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserPrincipal principal) {
        String roles = principal.getAuthorities().stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        return buildToken(principal, properties.getAccessTokenExpirationMs(), TYPE_ACCESS, roles);
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(principal, properties.getRefreshTokenExpirationMs(), TYPE_REFRESH, null);
    }

    private String buildToken(UserPrincipal principal, long ttlMs, String type, String roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        var builder = Jwts.builder()
                .setSubject(principal.getId().toString())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(CLAIM_TYPE, type)
                .claim(CLAIM_EMAIL, principal.getEmail());

        if (roles != null) {
            builder.claim(CLAIM_ROLES, roles);
        }
        return builder.signWith(signingKey).compact();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parse(token).getBody().getSubject());
    }

    public boolean isAccessToken(String token) {
        return TYPE_ACCESS.equals(parse(token).getBody().get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(String token) {
        return TYPE_REFRESH.equals(parse(token).getBody().get(CLAIM_TYPE, String.class));
    }

    public long getAccessTokenExpiresInSeconds() {
        return properties.getAccessTokenExpirationMs() / 1000;
    }

    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Token JWT expirado: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Token JWT nao suportado: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Token JWT malformado: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("Claims do JWT vazias: {}", ex.getMessage());
        } catch (JwtException ex) {
            log.warn("Assinatura/validacao do JWT falhou: {}", ex.getMessage());
        }
        return false;
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(properties.getIssuer())
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}
