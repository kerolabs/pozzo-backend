package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

/**
 * How the turns of a cycle are auctioned.
 *
 * @param closesDaysBeforeCutoff how many days before the cutoff of each period its auction closes
 * @param minimumBid             the smallest discount a member can offer
 * @param fallback               who gets the turn when nobody bids
 */
public record AuctionSettings(int closesDaysBeforeCutoff, Money minimumBid, AuctionFallback fallback) {

    public AuctionSettings {
        if (closesDaysBeforeCutoff < 0 || minimumBid == null || minimumBid.amount().signum() <= 0
                || fallback == null) {
            throw new IllegalArgumentException("Invalid auction settings");
        }
    }
}
