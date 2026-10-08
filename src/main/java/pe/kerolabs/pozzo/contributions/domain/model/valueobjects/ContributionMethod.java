package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * How a contribution was made: a transfer with a receipt, cash handed to the organizer, or covered by another member.
 */
public enum ContributionMethod {
    TRANSFER,
    CASH,
    COVERAGE
}
