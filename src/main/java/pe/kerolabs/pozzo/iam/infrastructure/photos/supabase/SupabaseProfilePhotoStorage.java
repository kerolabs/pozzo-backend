package pe.kerolabs.pozzo.iam.infrastructure.photos.supabase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.photos.ProfilePhotoStorage;
import pe.kerolabs.pozzo.shared.application.exceptions.ExternalServiceException;

import java.util.Map;
import java.util.UUID;

/**
 * Anti-corruption layer towards Supabase Storage: profile photos go to a public bucket of the same
 * Supabase project as the database, and the profile keeps the public URL.
 *
 * <p>It needs the project URL and a secret key with access to Storage. The bucket is created, public,
 * the first time a photo is stored. Failures are reported as {@link ExternalServiceException}.</p>
 */
@Slf4j
@Component
public class SupabaseProfilePhotoStorage implements ProfilePhotoStorage {

    private final String projectUrl;
    private final String bucket;
    private final RestClient restClient;
    private volatile boolean bucketReady;

    public SupabaseProfilePhotoStorage(@Value("${photos.supabase.url}") String projectUrl,
                                       @Value("${photos.supabase.secret-key}") String secretKey,
                                       @Value("${photos.supabase.bucket}") String bucket) {
        this.projectUrl = projectUrl.endsWith("/") ? projectUrl.substring(0, projectUrl.length() - 1) : projectUrl;
        this.bucket = bucket;
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
    public String store(UUID accountId, byte[] content, String contentType) {
        requireConfigured();
        ensureBucket();
        var path = "%s/%s.%s".formatted(accountId, UUID.randomUUID(), extensionOf(contentType));
        try {
            restClient.post()
                    .uri("/object/{bucket}/{path}", bucket, path)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("cache-control", "max-age=31536000")
                    .body(content)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ExternalServiceException("PHOTO_UPLOAD_FAILED", "Supabase Storage did not store the photo", e);
        }
        return "%s/storage/v1/object/public/%s/%s".formatted(projectUrl, bucket, path);
    }

    @Override
    public void delete(String publicUrl) {
        var prefix = "%s/storage/v1/object/public/%s/".formatted(projectUrl, bucket);
        if (projectUrl.isBlank() || publicUrl == null || !publicUrl.startsWith(prefix)) {
            return;
        }
        try {
            restClient.method(HttpMethod.DELETE)
                    .uri("/object/{bucket}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", new String[]{publicUrl.substring(prefix.length())}))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            // A leftover file does not break anything; the profile no longer points to it.
            log.warn("The previous profile photo could not be deleted: {}", e.getMessage());
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
                    .body(Map.of("id", bucket, "name", bucket, "public", true,
                            "allowed_mime_types", new String[]{"image/jpeg", "image/png", "image/webp"}))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        // 400 or 409 when the bucket already exists; anything else will fail on the upload.
                    })
                    .toBodilessEntity();
            bucketReady = true;
        } catch (RestClientResponseException e) {
            log.warn("The photos bucket could not be checked: {}", e.getMessage());
        } catch (RestClientException e) {
            throw new ExternalServiceException("PHOTO_UPLOAD_FAILED", "Supabase Storage did not answer", e);
        }
    }

    private void requireConfigured() {
        if (projectUrl.isBlank()) {
            throw new ExternalServiceException("PHOTO_STORAGE_NOT_CONFIGURED",
                    "Set SUPABASE_URL and SUPABASE_SECRET_KEY to store profile photos",
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
}
