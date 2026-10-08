package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Data read from a Yape or Plin receipt on the phone (with ML Kit) and confirmed by the member.
 * The image is validated on the phone; it is kept apart, in a private storage, once the contribution exists.
 *
 * @param operationNumber the operation number printed on the receipt
 * @param payerName       who paid, when the receipt shows it
 * @param payeeName       who received the money
 * @param amount          the amount paid
 * @param paidAt          the date of the payment
 * @param source          the application that issued the receipt
 */
public record PaymentReceipt(String operationNumber, @Nullable String payerName, String payeeName, Money amount,
                             LocalDate paidAt, ReceiptSource source) {

    public PaymentReceipt {
        if (operationNumber == null || operationNumber.isBlank()) {
            throw new IllegalArgumentException("The operation number is required");
        }
        if (payeeName == null || payeeName.isBlank()) {
            throw new IllegalArgumentException("The recipient of the payment is required");
        }
        if (amount == null || paidAt == null || source == null) {
            throw new IllegalArgumentException("The amount, the date and the source of the receipt are required");
        }
        operationNumber = operationNumber.strip();
        payeeName = payeeName.strip();
    }

    /**
     * Returns true when the recipient on the receipt is the expected one. Case, accents and extra
     * words are ignored, and wallets that mask the end of a name ("Anna Web*") are accepted, but
     * every word of the expected name has to be there: "Anna Weber R." matches "Anna Weber",
     * "Anita Weber R." does not.
     */
    public boolean matchesPayee(String expectedName) {
        var found = words(payeeName);
        return words(expectedName).stream().allMatch(expected -> found.stream().anyMatch(word ->
                word.equals(expected) || (word.endsWith("*") && expected.startsWith(word.replace("*", "")))));
    }

    private static List<String> words(String name) {
        var plain = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9* ]", " ")
                .trim();
        return plain.isEmpty() ? List.of() : Arrays.asList(plain.split("\\s+"));
    }
}
