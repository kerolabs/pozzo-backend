package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import java.time.LocalDate;

/**
 * Rules of the savings group copied when the group started; they no longer change.
 *
 * @param contribution          the amount each member contributes every period
 * @param periodicity           how often the members contribute
 * @param firstContributionDate the cutoff date of the first period
 * @param destinationMethod     the wallet where the contributions are sent (YAPE or PLIN)
 * @param destinationPhone      the wallet number, in E.164 format
 * @param payeeName             the name a valid receipt must show as recipient: the organizer's
 */
public record CycleRules(Money contribution, Periodicity periodicity, LocalDate firstContributionDate,
                         String destinationMethod, String destinationPhone, String payeeName) {

    public CycleRules {
        if (contribution == null || contribution.amount().signum() <= 0) {
            throw new IllegalArgumentException("The contribution must be greater than zero");
        }
        if (periodicity == null || firstContributionDate == null) {
            throw new IllegalArgumentException("The periodicity and the first contribution date are required");
        }
        if (destinationMethod == null || destinationPhone == null || payeeName == null) {
            throw new IllegalArgumentException("The destination of the contributions is required");
        }
    }

    /**
     * The cutoff date of a turn, counting the first one as 1.
     */
    public LocalDate cutoffDateOfTurn(int turnNumber) {
        return periodicity.advance(firstContributionDate, turnNumber - 1);
    }

    /**
     * The day a turn opens: the day after the cutoff of the previous turn. The first turn opens
     * one period before its cutoff.
     */
    public LocalDate opensAtOfTurn(int turnNumber) {
        return periodicity.advance(firstContributionDate, turnNumber - 2).plusDays(1);
    }

    /**
     * The day contributions are due: the day of the month for monthly cycles, or the day of the
     * week (1 Monday to 7 Sunday) for weekly and biweekly cycles.
     */
    public int cutoffDay() {
        return periodicity == Periodicity.MONTHLY
                ? firstContributionDate.getDayOfMonth()
                : firstContributionDate.getDayOfWeek().getValue();
    }
}
