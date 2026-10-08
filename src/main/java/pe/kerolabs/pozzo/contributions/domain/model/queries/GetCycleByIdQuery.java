package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for a cycle, for one of its members.
 */
public record GetCycleByIdQuery(UUID cycleId, UUID requesterAccountId) {
}
