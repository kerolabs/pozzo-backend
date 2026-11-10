package pe.kerolabs.pozzo.contributions.domain.model.commands;

import java.util.UUID;

/**
 * Gives the public link that proves a contribution was registered, for the member it counts for or the
 * organizer.
 */
public record ShareContributionCommand(UUID contributionId, UUID requesterAccountId) {
}
