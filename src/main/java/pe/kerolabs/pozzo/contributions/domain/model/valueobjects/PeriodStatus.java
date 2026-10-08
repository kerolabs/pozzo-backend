package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Lifecycle of a period: OPEN while contributions arrive, POT_COMPLETE when every member has paid or been covered, DELIVERED once the organizer hands over the pot.
 */
public enum PeriodStatus {
    OPEN,
    POT_COMPLETE,
    DELIVERED
}
