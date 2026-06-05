package com.medico.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * utilitarios para acessar a identidade autenticada no contexto corrente.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static Optional<UUID> getCurrentUserId() {
        return getCurrentPrincipal().map(UserPrincipal::getId);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentPrincipal().map(UserPrincipal::getEmail);
    }
}
