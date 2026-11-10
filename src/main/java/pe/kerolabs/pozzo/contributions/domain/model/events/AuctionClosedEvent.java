package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The auction of a period closed and its turn has a winner.
 */
public record AuctionClosedEvent(UUID auctionId, UUID cycleId, UUID periodId, int turnNumber,
                                 UUID winnerMembershipId, Instant occurredAt) {
}
