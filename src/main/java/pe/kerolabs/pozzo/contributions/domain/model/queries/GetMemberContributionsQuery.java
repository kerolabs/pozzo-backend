package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for the contributions of the requester in a cycle.
 */
public record GetMemberContributionsQuery(UUID cycleId, UUID requesterAccountId) {
}
