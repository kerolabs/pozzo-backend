package pe.kerolabs.pozzo.contributions.application.internal.outboundservices.receipts;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.shared.application.result.ApplicationError;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * What an image of a receipt or of the transfer of a pot must be, and how long the link to see it works.
 */
public final class ReceiptImageRules {

    public static final int MAX_BYTES = 2 * 1024 * 1024;
    public static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    public static final Duration LINK_VALIDITY = Duration.ofMinutes(15);

    private ReceiptImageRules() {
    }

    /**
     * Checks an uploaded image.
     *
     * @return the validation error, when the image breaks a rule
     */
    public static Optional<ApplicationError> check(byte[] content, @Nullable String contentType) {
        if (contentType == null || !TYPES.contains(contentType)) {
            return Optional.of(ApplicationError.validationError("image", "must be a JPEG, PNG or WebP image"));
        }
        if (content.length == 0 || content.length > MAX_BYTES) {
            return Optional.of(ApplicationError.validationError("image", "must weigh up to 2 MB"));
        }
        return Optional.empty();
    }
}
