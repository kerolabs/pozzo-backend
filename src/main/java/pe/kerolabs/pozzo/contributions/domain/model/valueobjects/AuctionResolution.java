package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * How the winner of an auction was decided.
 */
public enum AuctionResolution {
    /** The highest bid. */
    BID,
    /** A draw, because nobody bid. */
    DRAW,
    /** The organizer chose, because nobody bid. */
    ORGANIZER
}
