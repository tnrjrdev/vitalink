package com.vitalink.platform.messaging;

import com.vitalink.platform.config.AwsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

/**
 * Consumidor (subscriber) da fila SQS que recebe os eventos de dominio publicados
 * no SNS (fan-out SNS -> SQS). Faz long polling agendado e processa cada mensagem.
 *
 * Ativo apenas quando ha um SqsClient (app.aws.enabled=true) e o consumo esta
 * explicitamente ligado (app.aws.sqs.consumer-enabled=true). Sendo infraestrutura
 * de mensageria, fica fora da medicao de cobertura.
 */
@Slf4j
@Component
@ConditionalOnBean(SqsClient.class)
@ConditionalOnProperty(prefix = "app.aws.sqs", name = "consumer-enabled", havingValue = "true")
public class SqsEventConsumer {
    private final SqsClient sqsClient;
    private final AwsProperties properties;

    public SqsEventConsumer(SqsClient sqsClient, AwsProperties properties) {
        this.sqsClient = sqsClient;
        this.properties = properties;
        log.info("SQS consumer ATIVO. Fila: {}", properties.getSqs().getAppointmentQueueUrl());
    }

    @Scheduled(fixedDelayString = "${app.aws.sqs.poll-delay-ms:5000}")
    public void poll() {
        String queueUrl = properties.getSqs().getAppointmentQueueUrl();
        if (!StringUtils.hasText(queueUrl)) {
            return;
        }

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(properties.getSqs().getMaxMessages())
                .waitTimeSeconds(properties.getSqs().getWaitTimeSeconds())
                .build();

        List<Message> messages = sqsClient.receiveMessage(request).messages();
        for (Message message : messages) {
            try {
                handle(message);
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception ex) {
                log.error("Falha ao processar mensagem SQS {}: {}", message.messageId(), ex.getMessage());
            }
        }
    }

    private void handle(Message message) {
        log.info("Evento recebido da fila SQS: id={}, body={}", message.messageId(), message.body());
        // Ponto de extensao: disparar notificacoes, atualizar projecoes, etc.
    }
}
