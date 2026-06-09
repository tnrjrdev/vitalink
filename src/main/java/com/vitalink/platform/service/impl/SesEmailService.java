package com.vitalink.platform.service.impl;

import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.integration.EmailMessage;
import com.vitalink.platform.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.aws", name = "enabled", havingValue = "true")
public class SesEmailService implements EmailService {
    private final SesV2Client sesClient;
    private final AwsProperties properties;

    public SesEmailService(SesV2Client sesClient, AwsProperties properties) {
        this.sesClient = sesClient;
        this.properties = properties;
    }

    @Override
    public void send(EmailMessage message) {
        if (message == null || !StringUtils.hasText(message.getTo())) {
            log.debug("E-mail ignorado: destinatario ausente.");
            return;
        }

        Content bodyContent = Content.builder().data(message.getBody()).charset("UTF-8").build();
        Body body = message.isHtml()
                ? Body.builder().html(bodyContent).build()
                : Body.builder().text(bodyContent).build();

        Message sesMessage = Message.builder()
                .subject(Content.builder().data(message.getSubject()).charset("UTF-8").build())
                .body(body)
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .fromEmailAddress(properties.getSes().getFrom())
                .destination(Destination.builder().toAddresses(message.getTo()).build())
                .content(EmailContent.builder().simple(sesMessage).build())
                .build();

        try {
            sesClient.sendEmail(request);
            log.info("E-mail enviado via SES: to={}, subject='{}'", message.getTo(), message.getSubject());
        } catch (Exception ex) {
            // Side-effect nao deve quebrar a operacao de negocio principal.
            log.error("Falha ao enviar e-mail via SES (to={}): {}", message.getTo(), ex.getMessage());
        }
    }
}
