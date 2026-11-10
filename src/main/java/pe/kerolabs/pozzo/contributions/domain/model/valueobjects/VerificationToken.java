package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * The secret part of the public link that proves a contribution was registered. It cannot be guessed, so
 * only who received the link can open the record.
 *
 * @param value 24 characters of URL-safe Base64
 */
public record VerificationToken(String value) {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int BYTES = 18;

    public VerificationToken {
        if (value == null || !value.matches("[A-Za-z0-9_-]{16,32}")) {
            throw new IllegalArgumentException("Invalid verification token");
        }
    }

    public static VerificationToken random() {
        var bytes = new byte[BYTES];
        RANDOM.nextBytes(bytes);
        return new VerificationToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }
}
