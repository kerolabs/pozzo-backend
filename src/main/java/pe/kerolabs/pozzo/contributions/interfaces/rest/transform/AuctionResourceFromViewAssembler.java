package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.application.queryservices.AuctionView;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Bid;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.AuctionResource;

import java.util.UUID;

/**
 * Builds what a member sees of the auction of a period.
 */
public final class AuctionResourceFromViewAssembler {

    private AuctionResourceFromViewAssembler() {
    }

    public static AuctionResource toResourceFromView(AuctionView view, UUID requesterAccountId) {
        var auction = view.auction();
        var cycle = view.cycle();
        var mine = cycle.participantOf(requesterAccountId).map(CycleTurn::membershipId).orElse(null);
        var myBid = auction.getBids().stream().filter(bid -> bid.membershipId().equals(mine)).findFirst();
        var highest = auction.highestBid().map(Bid::membershipId).orElse(null);
        var canBid = auction.isOpen() && mine != null
                && cycle.stillToCollect().stream().anyMatch(turn -> turn.membershipId().equals(mine));
        var winner = auction.getWinnerMembershipId();
        return new AuctionResource(
                auction.getPeriodId(),
                auction.getTurnNumber(),
                auction.getStatus(),
                auction.getClosesOn(),
                auction.getMinimumBid().amount(),
                auction.getPotAmount().amount(),
                auction.getFallback(),
                canBid,
                myBid.map(bid -> bid.amount().amount()).orElse(null),
                mine != null && mine.equals(highest),
                auction.rankedBids().stream().map(bid -> new AuctionResource.BidItem(
                        bid.membershipId(),
                        cycle.findParticipant(bid.membershipId()).map(CycleTurn::displayName).orElse(""),
                        bid.amount().amount(),
                        auction.getPotAmount().minus(bid.amount()).amount(),
                        bid.placedAt())).toList(),
                winner,
                winner == null ? null : cycle.findParticipant(winner).map(CycleTurn::displayName).orElse(""),
                auction.getDiscount() == null ? null : auction.getDiscount().amount(),
                winner == null ? null : view.period().payoutAmount().amount(),
                auction.getResolution(),
                auction.getClosedAt());
    }
}
