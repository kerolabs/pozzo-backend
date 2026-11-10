package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Swaps the turns of two members whose periods have not started. Only the organizer.
 */
public record SwapTurnsCommand(UUID cycleId, UUID requesterAccountId, UUID firstMembershipId,
                               UUID secondMembershipId) {
}
