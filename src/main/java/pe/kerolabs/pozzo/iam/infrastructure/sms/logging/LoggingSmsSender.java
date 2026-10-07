package pe.kerolabs.pozzo.iam.infrastructure.sms.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * SMS sender that writes the message to the server log instead of sending it.
 * It is the default ({@code sms.provider=log}), meant for local development, where the
 * code can be read from the console.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "log", matchIfMissing = true)
public class LoggingSmsSender implements SmsSender {

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        log.info("SMS to {}: {}", phoneNumber.e164(), message);
    }
}
