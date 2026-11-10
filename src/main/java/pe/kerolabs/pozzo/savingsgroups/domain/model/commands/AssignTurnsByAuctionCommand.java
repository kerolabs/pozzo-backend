package pe.kerolabs.pozzo.savingsgroups.domain.model.commands;

import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.AuctionFallback;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Decides that the turns of a group are auctioned. Only the organizer, before the group starts.
 */
public record AssignTurnsByAuctionCommand(UUID groupId, UUID requesterId, int closesDaysBeforeCutoff,
                                          BigDecimal minimumBid, AuctionFallback fallback) {
}
