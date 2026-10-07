package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Whether a member has already settled what is expected in a period. A pending contribution whose cutoff has passed is shown as late, but that is derived, not stored.
 */
public enum ExpectedStatus {
    PENDING,
    PAID,
    COVERED
}
