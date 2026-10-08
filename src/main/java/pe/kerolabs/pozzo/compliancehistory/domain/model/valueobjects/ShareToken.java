package pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Random token of a shared history link; long enough that it cannot be guessed.
 *
 * @param value the URL-safe token
 */
public record ShareToken(String value) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BYTES = 18;

    public ShareToken {
        if (value == null || !value.matches("[A-Za-z0-9_-]{16,32}")) {
            throw new IllegalArgumentException("Invalid share token");
        }
    }

    public static ShareToken random() {
        var bytes = new byte[BYTES];
        RANDOM.nextBytes(bytes);
        return new ShareToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }
}
