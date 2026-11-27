package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Difference between what was expected and what the receipt shows in one field.
 *
 * @param field    AMOUNT, PAYEE or DATE; or EDITED_AMOUNT, EDITED_PAYEE, EDITED_DATE or EDITED_OPERATION when the
 *                 member changed what the application read from the receipt
 * @param expected the expected value; for DATE, the cutoff date the payment had to meet; for an EDITED_ field,
 *                 what the application read
 * @param found    the value registered
 */
public record Inconsistency(String field, String expected, String found) {

    public static final String AMOUNT = "AMOUNT";
    public static final String PAYEE = "PAYEE";
    public static final String DATE = "DATE";
    public static final String EDITED_AMOUNT = "EDITED_AMOUNT";
    public static final String EDITED_PAYEE = "EDITED_PAYEE";
    public static final String EDITED_DATE = "EDITED_DATE";
    public static final String EDITED_OPERATION = "EDITED_OPERATION";
}
