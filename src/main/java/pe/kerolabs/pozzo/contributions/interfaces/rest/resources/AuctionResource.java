package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionFallback;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionResolution;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.AuctionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The auction of the turn of a period, as every member of the cycle sees it.
 */
@Schema(name = "Auction", description = "Auction of the turn of a period")
public record AuctionResource(
        @Schema(description = "Period identifier") UUID periodId,
        @Schema(description = "Turn auctioned", example = "2") int turnNumber,
        @Schema(description = "OPEN or CLOSED") AuctionStatus status,
        @Schema(description = "Last day to bid") LocalDate closesOn,
        @Schema(description = "Smallest discount accepted", example = "20.00") BigDecimal minimumBid,
        @Schema(description = "Pot of the period", example = "600.00") BigDecimal potAmount,
        @Schema(description = "Who collects when nobody bids: DRAW or ORGANIZER") AuctionFallback fallback,
        @Schema(description = "Whether the requester has not collected and can bid") boolean canBid,
        @Schema(description = "The bid of the requester", nullable = true) BigDecimal myBid,
        @Schema(description = "Whether the bid of the requester is the highest") boolean myBidIsHighest,
        @Schema(description = "Every bid, from the highest to the lowest") List<BidItem> bids,
        @Schema(description = "Membership that won the turn", nullable = true) UUID winnerMembershipId,
        @Schema(description = "Name of the winner", nullable = true) String winnerName,
        @Schema(description = "Discount offered by the winner", nullable = true) BigDecimal discount,
        @Schema(description = "What the winner receives", nullable = true) BigDecimal payoutAmount,
        @Schema(description = "BID, DRAW or ORGANIZER", nullable = true) AuctionResolution resolution,
        @Schema(description = "When it closed", nullable = true) Instant closedAt) {

    @Schema(name = "AuctionBid", description = "A bid of the auction")
    public record BidItem(
            @Schema(description = "Membership of the bidder") UUID membershipId,
            @Schema(description = "Name of the bidder", example = "Sofia Gonzales") String displayName,
            @Schema(description = "Discount offered", example = "50.00") BigDecimal amount,
            @Schema(description = "What they would receive", example = "550.00") BigDecimal receives,
            @Schema(description = "When the bid was placed") Instant placedAt) {
    }
}
