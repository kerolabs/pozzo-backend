package pe.kerolabs.pozzo.contributions.infrastructure.receipts.supabase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageLink;
import pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts.ReceiptImageStorage;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Anti-corruption layer towards Supabase Storage: receipt images go to a private bucket of the same
 * Supabase project as the database, and are only read through signed URLs that expire.
 *
 * <p>It uses the project URL and secret key of the profile photos. The bucket is created, private, the
 * first time a receipt is stored. Failures are reported as {@link ExternalServiceException}.</p>
 */
@Slf4j
@Component
public class SupabaseReceiptImageStorage implements ReceiptImageStorage {

    private final String projectUrl;
    private final String bucket;
    private final RestClient restClient;
    private final Clock clock;
    private volatile boolean bucketReady;

    public SupabaseReceiptImageStorage(@Value("${photos.supabase.url}") String projectUrl,
                                       @Value("${photos.supabase.secret-key}") String secretKey,
                                       @Value("${receipts.supabase.bucket}") String bucket,
                                       Clock clock) {
        this.projectUrl = projectUrl.endsWith("/") ? projectUrl.substring(0, projectUrl.length() - 1) : projectUrl;
        this.bucket = bucket;
        this.clock = clock;
        this.restClient = RestClient.builder()
                .baseUrl(this.projectUrl + "/storage/v1")
                .defaultHeaders(headers -> {
                    headers.set("apikey", secretKey);
                    // The legacy service_role key is a JWT and also goes as bearer; the new secret keys only as apikey.
                    if (secretKey.startsWith("eyJ")) {
                        headers.setBearerAuth(secretKey);
                    }
                })
                .build();
    }

    @Override
    public String store(UUID contributionId, byte[] content, String contentType) {
        requireConfigured();
        ensureBucket();
        var path = "%s/%s.%s".formatted(contributionId, UUID.randomUUID(), extensionOf(contentType));
        try {
            restClient.post()
                    .uri("/object/{bucket}/{path}", bucket, path)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ExternalServiceException("RECEIPT_UPLOAD_FAILED", "Supabase Storage did not store the receipt", e);
        }
        return path;
    }

    @Override
    public ReceiptImageLink link(String path, Duration validity) {
        requireConfigured();
        try {
            var signed = restClient.post()
                    .uri("/object/sign/{bucket}/{path}", bucket, path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", validity.toSeconds()))
                    .retrieve()
                    .body(SignedUrl.class);
            if (signed == null || signed.signedURL() == null) {
                throw new ExternalServiceException("RECEIPT_LINK_FAILED", "Supabase Storage did not sign the receipt",
                        new IllegalStateException("Empty answer"));
            }
            return new ReceiptImageLink(projectUrl + "/storage/v1" + signed.signedURL(), clock.instant().plus(validity));
        } catch (RestClientException e) {
            throw new ExternalServiceException("RECEIPT_LINK_FAILED", "Supabase Storage did not sign the receipt", e);
        }
    }

    private void ensureBucket() {
        if (bucketReady) {
            return;
        }
        try {
            restClient.post()
                    .uri("/bucket")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("id", bucket, "name", bucket, "public", false,
                            "allowed_mime_types", new String[]{"image/jpeg", "image/png", "image/webp"}))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // 400 or 409 when the bucket already exists; anything else will fail on the upload.
                    })
                    .toBodilessEntity();
            bucketReady = true;
        } catch (RestClientResponseException e) {
            log.warn("The receipts bucket could not be checked: {}", e.getMessage());
        } catch (RestClientException e) {
            throw new ExternalServiceException("RECEIPT_UPLOAD_FAILED", "Supabase Storage did not answer", e);
        }
    }

    private void requireConfigured() {
        if (projectUrl.isBlank()) {
            throw new ExternalServiceException("RECEIPT_STORAGE_NOT_CONFIGURED",
                    "Set SUPABASE_URL and SUPABASE_SECRET_KEY to store receipt images",
                    new IllegalStateException("Supabase Storage is not configured"));
        }
    }

    private static String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private record SignedUrl(String signedURL) {
    }
}
