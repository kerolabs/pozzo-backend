package pe.kerolabs.pozzo.iam.infrastructure.sms.twilio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.SmsSender;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

/**
 * Anti-corruption layer towards Twilio: sends the SMS through the Twilio Messages REST API.
 *
 * <p>Active when {@code sms.provider=twilio}. It needs the account SID, the auth token and the
 * sender number of the Twilio account. A failure is reported as {@link ExternalServiceException},
 * so the code request is rolled back and the member can try again.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio")
public class TwilioSmsSender implements SmsSender {

    private static final String API_BASE_URL = "https://api.twilio.com/2010-04-01";

    private final RestClient restClient;
    private final String accountSid;
    private final String fromNumber;

    public TwilioSmsSender(@Value("${sms.twilio.account-sid}") String accountSid,
                           @Value("${sms.twilio.auth-token}") String authToken,
                           @Value("${sms.twilio.from-number}") String fromNumber) {
        this.accountSid = accountSid;
        this.fromNumber = fromNumber;
        this.restClient = RestClient.builder()
                .baseUrl(API_BASE_URL)
                .defaultHeaders(headers -> headers.setBasicAuth(accountSid, authToken))
                .build();
    }

    @Override
    public void send(PhoneNumber phoneNumber, String message) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", phoneNumber.e164());
        form.add("From", fromNumber);
        form.add("Body", message);
        try {
            restClient.post()
                    .uri("/Accounts/{accountSid}/Messages.json", accountSid)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
            log.info("SMS sent through Twilio to {}", mask(phoneNumber));
        } catch (RestClientException e) {
            throw new ExternalServiceException("SMS_DELIVERY_FAILED",
                    "Twilio did not accept the message for " + mask(phoneNumber), e);
        }
    }

    private static String mask(PhoneNumber phoneNumber) {
        var e164 = phoneNumber.e164();
        return e164.substring(0, 4) + "*****" + e164.substring(e164.length() - 3);
    }
}
