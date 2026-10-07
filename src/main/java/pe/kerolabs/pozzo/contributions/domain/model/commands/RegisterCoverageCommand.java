package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Command for the organizer to record that a member covered what another member owed.
 */
public record RegisterCoverageCommand(UUID periodId, UUID requesterAccountId, UUID membershipId,
                                      UUID coveredByMembershipId) {
}
