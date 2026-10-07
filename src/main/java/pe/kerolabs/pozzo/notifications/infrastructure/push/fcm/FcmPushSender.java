package pe.kerolabs.pozzo.notifications.infrastructure.push.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import pe.kerolabs.pozzo.notifications.application.internal.outboundservices.push.PushSender;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Anti-corruption layer towards Firebase Cloud Messaging (HTTP v1 API), authenticated with the
 * service account of the Firebase project. Active when {@code push.provider=fcm}.
 *
 * <p>The service account is read from {@code push.fcm.service-account}, either the JSON itself or
 * the JSON in Base64.</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "push.provider", havingValue = "fcm")
public class FcmPushSender implements PushSender {

    private static final String SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String API_BASE_URL = "https://fcm.googleapis.com/v1";

    private final GoogleCredentials credentials;
    private final String projectId;
    private final RestClient restClient;

    public FcmPushSender(@Value("${push.fcm.service-account}") String serviceAccount) {
        var json = serviceAccount.strip().startsWith("{")
                ? serviceAccount.strip().getBytes(StandardCharsets.UTF_8)
                : Base64.getDecoder().decode(serviceAccount.strip());
        try {
            var accountCredentials = ServiceAccountCredentials.fromStream(new ByteArrayInputStream(json));
            this.projectId = accountCredentials.getProjectId();
            this.credentials = accountCredentials.createScoped(List.of(SCOPE));
        } catch (IOException e) {
            throw new UncheckedIOException("The Firebase service account could not be read", e);
        }
        this.restClient = RestClient.builder().baseUrl(API_BASE_URL).build();
    }

    @Override
    public PushOutcome send(String pushToken, UUID notificationId, String title, String body,
                            @Nullable String deepLink) {
        var data = new HashMap<String, String>();
        data.put("notificationId", notificationId.toString());
        if (deepLink != null) {
            data.put("deepLink", deepLink);
        }
        var message = Map.of("message", Map.of(
                "token", pushToken,
                "notification", Map.of("title", title, "body", body),
                "data", data,
                "android", Map.of("priority", "high")));
        try {
            restClient.post()
                    .uri("/projects/{projectId}/messages:send", projectId)
                    .headers(headers -> headers.setBearerAuth(accessToken()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(message)
                    .retrieve()
                    .toBodilessEntity();
            return PushOutcome.DELIVERED;
        } catch (HttpClientErrorException e) {
            if (isInvalidToken(e)) {
                log.info("FCM rejected the token of a device: {}", e.getStatusCode());
                return PushOutcome.INVALID_TOKEN;
            }
            log.warn("FCM did not accept notification {}: {} {}", notificationId, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return PushOutcome.FAILED;
        } catch (RestClientException e) {
            log.warn("FCM could not be reached for notification {}", notificationId, e);
            return PushOutcome.FAILED;
        }
    }

    /**
     * FCM answers 404 UNREGISTERED for an uninstalled application, 403 SENDER_ID_MISMATCH for a token
     * of another project and 400 INVALID_ARGUMENT for a malformed token.
     */
    private static boolean isInvalidToken(HttpClientErrorException e) {
        var status = e.getStatusCode();
        var response = e.getResponseBodyAsString();
        return status.isSameCodeAs(HttpStatus.NOT_FOUND)
                || response.contains("UNREGISTERED")
                || response.contains("SENDER_ID_MISMATCH")
                || (status.isSameCodeAs(HttpStatus.BAD_REQUEST) && response.contains("registration token"));
    }

    private String accessToken() {
        try {
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new UncheckedIOException("The Firebase access token could not be obtained", e);
        }
    }
}
