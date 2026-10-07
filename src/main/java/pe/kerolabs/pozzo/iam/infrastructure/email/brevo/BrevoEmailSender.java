package pe.kerolabs.pozzo.iam.infrastructure.email.brevo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.email.EmailSender;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

import java.util.List;

/**
 * Anti-corruption layer towards Brevo: sends transactional emails through its HTTPS API. It is used
 * instead of SMTP because the free plan of Render blocks the SMTP ports.
 *
 * <p>Active when {@code email.provider=brevo}. It needs an API key and a sender address verified in
 * Brevo. A failure is reported as {@link ExternalServiceException}, so the request is rolled back and
 * the member can try again.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "email.provider", havingValue = "brevo")
public class BrevoEmailSender implements EmailSender {

    private static final String API_BASE_URL = "https://api.brevo.com/v3";

    private final RestClient restClient;
    private final Address sender;

    public BrevoEmailSender(@Value("${email.brevo.api-key}") String apiKey,
                            @Value("${email.from-address}") String fromAddress,
                            @Value("${email.from-name}") String fromName) {
        this.sender = new Address(fromAddress, fromName);
        this.restClient = RestClient.builder()
                .baseUrl(API_BASE_URL)
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Override
    public void send(String to, String subject, String text) {
        var request = new SendEmailRequest(sender, List.of(new Address(to, null)), subject, text);
        try {
            restClient.post()
                    .uri("/smtp/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email sent through Brevo to {}", mask(to));
        } catch (RestClientException e) {
            throw new ExternalServiceException("EMAIL_DELIVERY_FAILED",
                    "Brevo did not accept the email for " + mask(to), e);
        }
    }

    private static String mask(String email) {
        var at = email.indexOf('@');
        return at <= 1 ? "***" + email.substring(Math.max(at, 0)) : email.charAt(0) + "***" + email.substring(at);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Address(String email, String name) {
    }

    record SendEmailRequest(Address sender, List<Address> to, String subject, String textContent) {
    }
}
