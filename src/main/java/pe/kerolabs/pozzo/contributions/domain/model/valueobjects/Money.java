package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An amount of money with its currency. Contributions keeps its own copy instead of sharing the
 * one of Savings Groups, so each context can evolve its model on its own.
 *
 * @param amount   the amount, with two decimals; zero is allowed for an empty pot
 * @param currency the ISO 4217 currency code
 */
public record Money(BigDecimal amount, String currency) {

    public static final String PEN = "PEN";

    public Money {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("The amount cannot be negative");
        }
        if (amount.scale() > 2) {
            throw new IllegalArgumentException("The amount cannot have more than two decimals");
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("The currency must be an ISO 4217 code");
        }
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Adds another monetary amount to the current amount.
     * Both values must use the same currency.
     */


    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }
    /**
     * Subtracts another amount from the current amount.
     * The resulting amount cannot be negative and is limited to zero.
     */

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount).max(BigDecimal.ZERO), currency);
    }
    /**
     * Multiplies the current monetary amount by an integer.
     */

    public Money times(int times) {
        return new Money(amount.multiply(BigDecimal.valueOf(times)), currency);
    }

    /**
     * Compares the amounts ignoring the scale, so 300 and 300.00 are the same amount.
     */
    public boolean isSameAmountAs(Money other) {
        return currency.equals(other.currency) && amount.compareTo(other.amount) == 0;
    }

    @Override
    public String toString() {
        return "S/ " + amount.toPlainString();
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot combine amounts in different currencies");
        }
    }
}
