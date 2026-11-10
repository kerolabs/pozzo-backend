package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Who collects when the organizer closes an auction nobody bid in.
 */
@Schema(name = "CloseAuction", description = "Who collects when nobody bid; ignored when there are bids")
public record CloseAuctionResource(
        @Schema(description = "Membership the organizer chooses", nullable = true) UUID winnerMembershipId) {
}
