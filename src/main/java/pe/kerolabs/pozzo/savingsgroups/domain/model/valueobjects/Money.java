package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount of money with its currency. Savings groups work in soles (PEN).
 *
 * @param amount   the amount, with two decimals
 * @param currency the ISO 4217 currency code
 */
public record Money(BigDecimal amount, String currency) {

    public static final String PEN = "PEN";

    public Money {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("The amount cannot have more than two decimals");
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("The currency must be an ISO 4217 code");
        }
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money soles(BigDecimal amount) {
        return new Money(amount, PEN);
    }

    /**
     * Returns this amount multiplied by a number of times, e.g. the contribution by the number of members.
     */
    public Money times(int times) {
        return new Money(amount.multiply(BigDecimal.valueOf(times)), currency);
    }
}
