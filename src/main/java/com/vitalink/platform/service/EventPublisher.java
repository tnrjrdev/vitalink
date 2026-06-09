package com.vitalink.platform.service;

import com.vitalink.platform.integration.DomainEvent;

/**
 * Porta de publicacao de eventos de dominio. Implementacao concreta: AWS SNS
 * (app.aws.enabled=true) ou um adaptador que apenas registra em log (fallback dev).
 */
public interface EventPublisher {
    void publish(DomainEvent event);
}
