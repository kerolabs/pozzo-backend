package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

/**
 * Plan of a savings group. The free plan covers one active group per organizer, of up to six members, with
 * the turns drawn or agreed. The paid plan, a fixed fee per group, lifts those limits and enables the auction.
 * There is no real charge yet: the paid plan is activated as a demonstration.
 */
public enum GroupPlan {
    FREE,
    PAID;

    /** Members a group of the free plan can have. */
    public static final int FREE_MAX_SEATS = 6;
}
