package com.vitalink.platform.config;

import com.vitalink.platform.security.SecurityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(SecurityUtils.getCurrentUserEmail().orElse("system"));
    }
}
