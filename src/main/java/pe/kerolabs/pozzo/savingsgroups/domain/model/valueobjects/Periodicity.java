package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * How often the members contribute and one of them collects the pot.
 */
public enum Periodicity {
    WEEKLY,
    BIWEEKLY,
    MONTHLY;

    /**
     * Returns the date that comes a number of periods after the start date.
     * For monthly groups the day of the month is kept, or the last day when the month is shorter.
     *
     * @param start   the first cutoff date
     * @param periods how many periods to move forward
     * @return the cutoff date of that period
     */
    public LocalDate advance(LocalDate start, int periods) {
        return switch (this) {
            case WEEKLY -> start.plusWeeks(periods);
            case BIWEEKLY -> start.plusWeeks(2L * periods);
            case MONTHLY -> start.plusMonths(periods);
        };
    }
}
