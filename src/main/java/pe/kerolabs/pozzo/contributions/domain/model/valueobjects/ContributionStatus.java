package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * State of a contribution. A receipt that matches what is expected is VALIDATED at once; one that does not is INCONSISTENT until the organizer APPROVES or REJECTS it.
 */
public enum ContributionStatus {
    VALIDATED,
    INCONSISTENT,
    APPROVED,
    REJECTED
}
