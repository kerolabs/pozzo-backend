package pe.kerolabs.pozzo.iam.infrastructure.verification.twilio;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.verification.VerificationCodeChannel;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

/**
 * Anti-corruption layer towards Twilio Verify: Twilio generates the code, sends it by SMS from its
 * own numbers and checks it, so Pozzo needs no phone number of its own.
 *
 * <p>Active when {@code sms.provider=twilio-verify}. Pozzo stores a marker instead of a hash because
 * it never learns the code; the limits of the aggregate still apply on top of Twilio's.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "twilio-verify")
public class TwilioVerifyCodeChannel implements VerificationCodeChannel {

    static final String STORED_MARKER = "twilio-verify";
    private static final String API_BASE_URL = "https://verify.twilio.com/v2";
    private static final String APPROVED = "approved";

    private final RestClient restClient;
    private final String serviceSid;

    public TwilioVerifyCodeChannel(@Value("${sms.twilio.account-sid}") String accountSid,
                                   @Value("${sms.twilio.auth-token}") String authToken,
                                   @Value("${sms.twilio.verify-service-sid}") String serviceSid) {
        this.serviceSid = serviceSid;
        this.restClient = RestClient.builder()
                .baseUrl(API_BASE_URL)
                .defaultHeaders(headers -> headers.setBasicAuth(accountSid, authToken))
                .build();
    }

    @Override
    public String deliver(PhoneNumber phoneNumber) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", phoneNumber.e164());
        form.add("Channel", "sms");
        form.add("Locale", "es");
        try {
            restClient.post()
                    .uri("/Services/{serviceSid}/Verifications", serviceSid)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Verification code sent through Twilio Verify to {}", mask(phoneNumber));
            return STORED_MARKER;
        } catch (RestClientException e) {
            throw new ExternalServiceException("SMS_DELIVERY_FAILED",
                    "Twilio Verify did not send the code to " + mask(phoneNumber), e);
        }
    }

    @Override
    public boolean check(PhoneNumber phoneNumber, String input, String storedValue) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", phoneNumber.e164());
        form.add("Code", input);
        try {
            var check = restClient.post()
                    .uri("/Services/{serviceSid}/VerificationCheck", serviceSid)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(VerificationCheck.class);
            return check != null && APPROVED.equals(check.status());
        } catch (HttpClientErrorException.NotFound e) {
            // Twilio no longer has a pending verification for the number: expired or already approved.
            return false;
        } catch (RestClientException e) {
            throw new ExternalServiceException("SMS_DELIVERY_FAILED",
                    "Twilio Verify did not check the code of " + mask(phoneNumber), e);
        }
    }

    private static String mask(PhoneNumber phoneNumber) {
        var e164 = phoneNumber.e164();
        return e164.substring(0, 4) + "*****" + e164.substring(e164.length() - 3);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VerificationCheck(String status) {
    }
}
