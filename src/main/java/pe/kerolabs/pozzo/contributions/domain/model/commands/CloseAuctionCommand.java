package pe.kerolabs.pozzo.contributions.domain.model.commands;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * The organizer closes the auction of a period.
 *
 * @param winnerMembershipId the member the organizer chooses when nobody bid and the group decides so
 */
public record CloseAuctionCommand(UUID periodId, UUID requesterAccountId, @Nullable UUID winnerMembershipId) {
}
