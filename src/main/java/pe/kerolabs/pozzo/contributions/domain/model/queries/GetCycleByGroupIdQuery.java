package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for the cycle of a savings group, for one of its members.
 */
public record GetCycleByGroupIdQuery(UUID groupId, UUID requesterAccountId) {
}
