package com.vitalink.platform.service;

import com.vitalink.platform.integration.EmailMessage;

/**
 * Porta de envio de e-mails transacionais. Implementacao concreta: AWS SES
 * (app.aws.enabled=true) ou um adaptador que apenas registra em log (fallback dev).
 */
public interface EmailService {
    void send(EmailMessage message);
}
