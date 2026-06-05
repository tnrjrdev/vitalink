package com.medico.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * beans utilitarios de infraestrutura da aplicacao.
 */
@Configuration
public class AppConfig {

    /**
     * relogio injetavel (UTC). Centralizar o acesso ao "agora" torna as regras
     * de negocio dependentes de tempo deterministicas e testaveis.
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
