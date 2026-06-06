package com.vitalink.platform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.integration.DomainEvent;
import com.vitalink.platform.service.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "true")
public class SnsEventPublisher implements EventPublisher {
    private final SnsClient snsClient;
    private final AwsProperties properties;
    private final ObjectMapper objectMapper;

    public SnsEventPublisher(SnsClient snsClient, AwsProperties properties, ObjectMapper objectMapper) {
        this.snsClient = snsClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        String topicArn = properties.getSns().getAppointmentTopicArn();
        if (!StringUtils.hasText(topicArn)) {
            log.warn("Topico SNS nao configurado (app.aws.sns.appointment-topic-arn). Evento '{}' nao publicado.",
                    event.getType());
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(event.getAttributes());
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject(event.getType())
                    .message(payload)
                    .messageAttributes(Map.of("eventType",
                            MessageAttributeValue.builder().dataType("String").stringValue(event.getType()).build()))
                    .build();
            snsClient.publish(request);
            log.info("Evento publicado no SNS: type={}, topic={}", event.getType(), topicArn);
        } catch (Exception ex) {
            // Side-effect nao deve quebrar a operacao de negocio principal.
            log.error("Falha ao publicar evento '{}' no SNS: {}", event.getType(), ex.getMessage());
        }
    }
}
