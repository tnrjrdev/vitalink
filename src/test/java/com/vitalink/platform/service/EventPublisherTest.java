package com.vitalink.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.integration.DomainEvent;
import com.vitalink.platform.service.impl.LogEventPublisher;
import com.vitalink.platform.service.impl.SnsEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisher")
class EventPublisherTest {
    @Mock private SnsClient snsClient;

    private SnsEventPublisher snsPublisher(String topicArn) {
        AwsProperties properties = new AwsProperties();
        properties.getSns().setAppointmentTopicArn(topicArn);
        return new SnsEventPublisher(snsClient, properties, new ObjectMapper());
    }

    private DomainEvent sampleEvent() {
        return DomainEvent.builder().type("appointment.scheduled").attribute("id", "1").build();
    }

    @Test
    @DisplayName("SNS publica quando o topico esta configurado")
    void snsShouldPublish() {
        snsPublisher("arn:aws:sns:us-east-1:000:topic").publish(sampleEvent());
        verify(snsClient).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("SNS nao publica quando o topico esta ausente")
    void snsShouldSkipWithoutTopic() {
        snsPublisher("  ").publish(sampleEvent());
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("SNS nao propaga falha da publicacao")
    void snsShouldSwallowFailure() {
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("sns down"));
        assertThatCode(() -> snsPublisher("arn:topic").publish(sampleEvent())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Fallback de log nao lanca excecao")
    void logShouldNotThrow() {
        LogEventPublisher service = new LogEventPublisher();
        assertThatCode(() -> service.publish(sampleEvent())).doesNotThrowAnyException();
        assertThatCode(() -> service.publish(null)).doesNotThrowAnyException();
    }
}
