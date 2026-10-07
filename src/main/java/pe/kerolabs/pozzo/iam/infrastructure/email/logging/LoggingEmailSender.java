package pe.kerolabs.pozzo.iam.infrastructure.email.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.email.EmailSender;

/**
 * Email sender that writes the message to the server log instead of sending it.
 * It is the default ({@code email.provider=log}), meant for local development.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "log", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String text) {
        log.info("Email to {} ({}): {}", to, subject, text);
    }
}
