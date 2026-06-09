package com.vitalink.platform.service.impl;

import com.vitalink.platform.integration.DomainEvent;
import com.vitalink.platform.service.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Fallback de publicacao de eventos para desenvolvimento sem AWS: apenas
 * registra o evento em log. Ativo quando app.aws.enabled e falso.
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LogEventPublisher implements EventPublisher {
    @Override
    public void publish(DomainEvent event) {
        if (event == null) {
            return;
        }
        log.info("[EVENT-LOCAL] type={}, attributes={}", event.getType(), event.getAttributes());
    }
}
