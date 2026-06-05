package com.medico.platform.config;

import com.medico.platform.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * habilita a auditoria do Spring Data JPA (preenchimento automatico de
 * createdAt/updatedAt e createdBy/updatedBy nas entidades que estendem
 * {@code BaseEntity}).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * fornece o identificador do autor das operacoes (e-mail do usuario
     * autenticado), ou "system" para operacoes sem contexto de seguranca
     * (ex.: carga inicial / jobs).
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SecurityUtils.getCurrentUserEmail().orElse("system"));
    }
}
