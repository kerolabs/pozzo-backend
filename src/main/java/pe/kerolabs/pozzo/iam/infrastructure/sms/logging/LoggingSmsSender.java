package pe.kerolabs.pozzo.iam.infrastructure.sms.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

/**
 * SMS sender that writes the message to the server log instead of sending it.
 * Used until the adapter to a real SMS provider is in place, so the access flow
 * can be tested by reading the code from the log.
 */
@Slf4j
@Component
public class LoggingSmsSender implements SmsSender {

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        log.info("SMS to {}: {}", phoneNumber.e164(), message);
    }
}
