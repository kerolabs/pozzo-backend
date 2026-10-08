package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for the period in progress of a cycle, with the state of every contribution.
 */
public record GetCurrentPeriodQuery(UUID cycleId, UUID requesterAccountId) {
}
