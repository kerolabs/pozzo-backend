package pe.kerolabs.pozzo.iam.infrastructure.sms.smsgate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.domain.model.aggregates.VerificationCode;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

import java.util.List;

/**
 * Anti-corruption layer towards SMS Gate (sms-gate.app): an Android phone with the SMS Gate app sends
 * the SMS from its own SIM, and the backend hands the message to the phone through the SMS Gate cloud
 * server.
 *
 * <p>Active when {@code sms.provider=smsgate}. It needs the username and password the app shows in its
 * Cloud Server section. The message expires with the code, so a phone that was offline does not send
 * a stale code later. A failure is reported as {@link ExternalServiceException}, so the code request is
 * rolled back and the member can try again.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "smsgate")
public class SmsGateSmsSender implements SmsSender {

    /** Priorities of 100 or more skip the queue of the phone. */
    private static final int EXPEDITED_PRIORITY = 100;

    private final RestClient restClient;

    public SmsGateSmsSender(@Value("${sms.smsgate.base-url}") String baseUrl,
                            @Value("${sms.smsgate.username}") String username,
                            @Value("${sms.smsgate.password}") String password) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(username, password))
                .build();
    }

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        var request = new SendMessageRequest(
                new TextMessage(message),
                List.of(phoneNumber.e164()),
                VerificationCode.VALIDITY.toSeconds(),
                EXPEDITED_PRIORITY);
        try {
            restClient.post()
                    .uri("/messages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("SMS handed to SMS Gate for {}", mask(phoneNumber));
        } catch (RestClientException e) {
            throw new ExternalServiceException("SMS_DELIVERY_FAILED",
                    "SMS Gate did not accept the message for " + mask(phoneNumber), e);
        }
    }

    private static String mask(PhoneNumber phoneNumber) {
        var e164 = phoneNumber.e164();
        return e164.substring(0, 4) + "*****" + e164.substring(e164.length() - 3);
    }

    record TextMessage(String text) {
    }

    record SendMessageRequest(TextMessage textMessage, List<String> phoneNumbers, long ttl, int priority) {
    }
}
