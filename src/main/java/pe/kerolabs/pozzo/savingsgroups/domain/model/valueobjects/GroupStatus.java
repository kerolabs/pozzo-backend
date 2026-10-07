package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

/**
 * Lifecycle of a savings group: DRAFT while it is being set up, READY when it can start,
 * STARTED once the cycle runs and CLOSED when every member has collected.
 */
public enum GroupStatus {
    DRAFT,
    READY,
    STARTED,
    CLOSED
}
