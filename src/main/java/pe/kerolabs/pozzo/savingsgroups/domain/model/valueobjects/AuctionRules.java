package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

/**
 * How the turns of a group are auctioned: each period, the members who have not collected bid a discount on
 * the pot, and the highest bid collects.
 *
 * @param closesDaysBeforeCutoff how many days before the cutoff of each period its auction closes
 * @param minimumBid             the smallest discount a member can offer
 * @param fallback               who gets the turn when nobody bids
 */
public record AuctionRules(int closesDaysBeforeCutoff, Money minimumBid, AuctionFallback fallback) {

    public static final int MAX_DAYS_BEFORE_CUTOFF = 30;

    public AuctionRules {
        if (closesDaysBeforeCutoff < 0 || closesDaysBeforeCutoff > MAX_DAYS_BEFORE_CUTOFF) {
            throw new IllegalArgumentException(
                    "The auction must close between 0 and %d days before the cutoff".formatted(MAX_DAYS_BEFORE_CUTOFF));
        }
        if (minimumBid == null || minimumBid.amount().signum() <= 0) {
            throw new IllegalArgumentException("The minimum bid must be greater than zero");
        }
        if (fallback == null) {
            throw new IllegalArgumentException("Choose what happens when nobody bids");
        }
    }
}
