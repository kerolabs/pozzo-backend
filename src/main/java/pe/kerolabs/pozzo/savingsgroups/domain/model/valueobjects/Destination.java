package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import java.util.regex.Pattern;

/**
 * Where the contributions are sent: a Yape or Plin wallet identified by a Peruvian mobile number.
 * Pozzo checks each receipt against it; it never receives or holds money.
 *
 * @param method      the wallet
 * @param phoneNumber the mobile number of the wallet, in E.164 format
 */
public record Destination(PaymentMethod method, String phoneNumber) {

    private static final Pattern PERUVIAN_MOBILE_E164 = Pattern.compile("\\+519\\d{8}");

    public Destination {
        if (method == null) {
            throw new IllegalArgumentException("The payment method of the destination is required");
        }
        if (phoneNumber == null || !PERUVIAN_MOBILE_E164.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("The destination must be a Peruvian mobile number");
        }
    }

    /**
     * Creates a destination from the nine national digits of the number; spaces are ignored.
     */
    public static Destination of(PaymentMethod method, String nationalNumber) {
        var digits = nationalNumber == null ? "" : nationalNumber.replace(" ", "");
        return new Destination(method, "+51" + digits);
    }
}
