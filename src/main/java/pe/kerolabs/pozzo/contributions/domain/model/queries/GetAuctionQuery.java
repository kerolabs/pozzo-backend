package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * The auction of a period, for a member of the cycle.
 */
public record GetAuctionQuery(UUID periodId, UUID requesterAccountId) {
}
