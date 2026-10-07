package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rules of a savings group: how much each member contributes, how often, how many members there are,
 * when the first contribution is due and where the contributions are sent.
 *
 * <p>The cycle lasts one period per member, and each member collects the pot once.</p>
 *
 * @param contribution          the fixed amount each member contributes every period
 * @param periodicity           how often the members contribute
 * @param seats                 the number of members, which is also the number of periods
 * @param firstContributionDate the cutoff date of the first period
 * @param destination           where the contributions are sent, required before the group starts
 */
public record GroupRules(Money contribution, Periodicity periodicity, int seats, LocalDate firstContributionDate,
                         @Nullable Destination destination) {

    public static final int MIN_SEATS = 2;
    public static final int MAX_SEATS = 50;

    public GroupRules {
        if (contribution == null) {
            throw new IllegalArgumentException("The contribution amount is required");
        }
        if (periodicity == null) {
            throw new IllegalArgumentException("The periodicity is required");
        }
        if (seats < MIN_SEATS || seats > MAX_SEATS) {
            throw new IllegalArgumentException(
                    "The number of members must be between %d and %d".formatted(MIN_SEATS, MAX_SEATS));
        }
        if (firstContributionDate == null) {
            throw new IllegalArgumentException("The first contribution date is required");
        }
    }

    public GroupRules withDestination(Destination destination) {
        return new GroupRules(contribution, periodicity, seats, firstContributionDate, destination);
    }

    /**
     * The amount one member collects in a period: the contribution times the number of members.
     */
    public Money pot() {
        return contribution.times(seats);
    }

    /**
     * The day contributions are due: the day of the month for monthly groups,
     * or the day of the week (1 Monday to 7 Sunday) for weekly and biweekly groups.
     */
    public int cutoffDay() {
        return periodicity == Periodicity.MONTHLY
                ? firstContributionDate.getDayOfMonth()
                : firstContributionDate.getDayOfWeek().getValue();
    }

    public DayOfWeek cutoffDayOfWeek() {
        return firstContributionDate.getDayOfWeek();
    }

    /**
     * The cutoff date of a turn, counting the first one as 1.
     */
    public LocalDate cutoffDateOfTurn(int turnNumber) {
        return periodicity.advance(firstContributionDate, turnNumber - 1);
    }

    public boolean hasDestination() {
        return destination != null;
    }
}
