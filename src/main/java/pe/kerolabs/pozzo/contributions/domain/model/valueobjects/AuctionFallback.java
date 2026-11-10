package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * Who gets the turn when an auction closes without bids.
 */
public enum AuctionFallback {
    /** A draw among the members who have not collected. */
    DRAW,
    /** The organizer chooses among the members who have not collected. */
    ORGANIZER
}
