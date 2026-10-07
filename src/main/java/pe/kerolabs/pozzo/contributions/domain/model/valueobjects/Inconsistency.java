package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Difference between what was expected and what the receipt shows in one field.
 *
 * @param field    AMOUNT, PAYEE or DATE
 * @param expected the expected value, as shown to the organizer
 * @param found    the value read from the receipt
 */
public record Inconsistency(String field, String expected, String found) {

    public static final String AMOUNT = "AMOUNT";
    public static final String PAYEE = "PAYEE";
    public static final String DATE = "DATE";
}
