package com.vitalink.platform.service;

import com.vitalink.platform.config.AwsProperties;
import com.vitalink.platform.integration.EmailMessage;
import com.vitalink.platform.service.impl.LogEmailService;
import com.vitalink.platform.service.impl.SesEmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceTest {
    @Mock private SesV2Client sesClient;

    private SesEmailService sesEmailService() {
        AwsProperties properties = new AwsProperties();
        properties.getSes().setFrom("no-reply@vitalink.com");
        return new SesEmailService(sesClient, properties);
    }

    @Test
    @DisplayName("SES envia e-mail quando ha destinatario")
    void sesShouldSend() {
        sesEmailService().send(EmailMessage.builder()
                .to("paciente@test.com").subject("Oi").body("corpo").build());
        verify(sesClient).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("SES ignora mensagem sem destinatario")
    void sesShouldSkipWithoutRecipient() {
        sesEmailService().send(EmailMessage.builder().subject("Oi").body("corpo").build());
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("SES nao propaga falha do envio")
    void sesShouldSwallowFailure() {
        when(sesClient.sendEmail(any(SendEmailRequest.class))).thenThrow(new RuntimeException("ses down"));
        assertThatCode(() -> sesEmailService().send(EmailMessage.builder()
                .to("p@test.com").subject("s").body("b").build())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Fallback de log nao lanca excecao")
    void logShouldNotThrow() {
        LogEmailService service = new LogEmailService();
        assertThatCode(() -> service.send(EmailMessage.builder()
                .to("p@test.com").subject("s").body("b").build())).doesNotThrowAnyException();
        assertThatCode(() -> service.send(null)).doesNotThrowAnyException();
    }
}
