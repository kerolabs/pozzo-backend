package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.events.AuctionClosedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.AuctionOpenedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionFallback;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionResolution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Bid;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Auction aggregate root: the members who have not collected offer a discount on the pot of a period to
 * collect in its turn. The highest discount wins, and the first one placed breaks a tie. The discount is shared
 * by the members who still have not collected, as a credit on their next contribution.
 */
@Getter
public class Auction extends AbstractDomainAggregateRoot<Auction> {

    private UUID id;
    private UUID cycleId;
    private UUID periodId;
    private int turnNumber;
    private LocalDate closesOn;
    private Money minimumBid;
    private Money potAmount;
    private AuctionFallback fallback;
    private AuctionStatus status;
    private List<Bid> bids = new ArrayList<>();
    private @Nullable UUID winnerMembershipId;
    private @Nullable Money discount;
    private @Nullable Money creditPerMember;
    private @Nullable AuctionResolution resolution;
    private @Nullable Instant closedAt;

    public Auction() {
    }

    /**
     * Opens the auction of a period that has just opened.
     */
    public static Auction open(Cycle cycle, Period period, Instant now) {
        var settings = cycle.getAuctionSettings();
        if (settings == null) {
            throw new IllegalStateException("The cycle does not auction its turns");
        }
        var auction = new Auction();
        auction.id = UUID.randomUUID();
        auction.cycleId = cycle.getId();
        auction.periodId = period.getId();
        auction.turnNumber = period.getTurnNumber();
        var closesOn = period.getCutoffDate().minusDays(settings.closesDaysBeforeCutoff());
        auction.closesOn = closesOn.isBefore(period.getOpensAt()) ? period.getOpensAt() : closesOn;
        auction.minimumBid = settings.minimumBid();
        auction.potAmount = period.potAmount();
        auction.fallback = settings.fallback();
        auction.status = AuctionStatus.OPEN;
        auction.registerDomainEvent(new AuctionOpenedEvent(auction.id, auction.cycleId, auction.periodId,
                auction.turnNumber, auction.closesOn, now));
        return auction;
    }

    /**
     * Places or replaces the bid of a member. Only the latest bid of each member counts.
     *
     * @param eligible the memberships that have not collected yet
     */
    public void placeBid(UUID membershipId, Money amount, Collection<UUID> eligible, LocalDate today, Instant now) {
        if (!isOpen() || today.isAfter(closesOn)) {
            throw new BusinessRuleViolationException("AUCTION_CLOSED", "The auction of this turn is closed");
        }
        if (!eligible.contains(membershipId)) {
            throw new BusinessRuleViolationException("ALREADY_COLLECTED",
                    "Only the members who have not collected can bid");
        }
        if (amount.amount().compareTo(minimumBid.amount()) < 0) {
            throw new BusinessRuleViolationException("BID_BELOW_MINIMUM",
                    "The bid must be at least " + minimumBid);
        }
        if (amount.amount().compareTo(potAmount.amount()) >= 0) {
            throw new BusinessRuleViolationException("BID_TOO_HIGH", "The bid must be less than the pot");
        }
        bids.removeIf(bid -> bid.membershipId().equals(membershipId));
        bids.add(new Bid(membershipId, amount, now));
    }

    /**
     * The winning bid so far: the highest, and the first one placed when two are equal.
     */
    public Optional<Bid> highestBid() {
        return bids.stream().max(Comparator.comparing((Bid bid) -> bid.amount().amount())
                .thenComparing(Bid::placedAt, Comparator.reverseOrder()));
    }

    /**
     * The bids from the highest to the lowest, as every member sees them.
     */
    public List<Bid> rankedBids() {
        return bids.stream().sorted(Comparator.comparing((Bid bid) -> bid.amount().amount()).reversed()
                .thenComparing(Bid::placedAt)).toList();
    }

    /**
     * Gives the turn to its winner. With bids the discount is the winning bid and it is shared, as a credit, by the
     * members who still have not collected; without bids there is no discount.
     *
     * @param othersStillToCollect how many members, besides the winner, have not collected
     */
    public void close(UUID winnerMembershipId, AuctionResolution resolution, int othersStillToCollect, Instant now) {
        if (!isOpen()) {
            throw new BusinessRuleViolationException("AUCTION_CLOSED", "The auction of this turn is closed");
        }
        var winningBid = resolution == AuctionResolution.BID ? highestBid().orElseThrow() : null;
        this.winnerMembershipId = winnerMembershipId;
        this.discount = winningBid == null ? Money.zero(potAmount.currency()) : winningBid.amount();
        this.creditPerMember = othersStillToCollect <= 0
                ? Money.zero(potAmount.currency())
                : Money.of(discount.amount().divide(BigDecimal.valueOf(othersStillToCollect), 2,
                        RoundingMode.HALF_UP), discount.currency());
        this.resolution = resolution;
        this.status = AuctionStatus.CLOSED;
        this.closedAt = now;
        registerDomainEvent(new AuctionClosedEvent(id, cycleId, periodId, turnNumber, winnerMembershipId, now));
    }

    public boolean isOpen() {
        return status == AuctionStatus.OPEN;
    }

    /**
     * Whether the closing date passed and the auction can close by itself.
     */
    public boolean isDue(LocalDate today) {
        return isOpen() && today.isAfter(closesOn);
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID cycleId, UUID periodId, int turnNumber, LocalDate closesOn,
                             Money minimumBid, Money potAmount, AuctionFallback fallback, AuctionStatus status,
                             List<Bid> bids, @Nullable UUID winnerMembershipId, @Nullable Money discount,
                             @Nullable Money creditPerMember, @Nullable AuctionResolution resolution,
                             @Nullable Instant closedAt) {
        this.id = id;
        this.cycleId = cycleId;
        this.periodId = periodId;
        this.turnNumber = turnNumber;
        this.closesOn = closesOn;
        this.minimumBid = minimumBid;
        this.potAmount = potAmount;
        this.fallback = fallback;
        this.status = status;
        this.bids = new ArrayList<>(bids);
        this.winnerMembershipId = winnerMembershipId;
        this.discount = discount;
        this.creditPerMember = creditPerMember;
        this.resolution = resolution;
        this.closedAt = closedAt;
    }
}
