package com.vitalink.platform.service.impl;

import com.vitalink.platform.integration.EmailMessage;
import com.vitalink.platform.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback de e-mail para desenvolvimento sem AWS: apenas registra o conteudo
 * em log, permitindo validar o fluxo sem SES. Ativo quando app.aws.enabled e falso.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LogEmailService implements EmailService {
    @Override
    public void send(EmailMessage message) {
        if (message == null) {
            return;
        }
        log.info("[EMAIL-LOCAL] to={}, subject='{}', body='{}'",
                message.getTo(), message.getSubject(), message.getBody());
    }
}
