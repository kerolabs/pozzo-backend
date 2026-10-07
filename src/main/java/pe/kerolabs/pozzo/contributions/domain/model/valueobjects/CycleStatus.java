package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Lifecycle of a cycle: ACTIVE while there are turns to collect, CLOSED after the last pot is delivered.
 */
public enum CycleStatus {
    ACTIVE,
    CLOSED
}
