package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for every period of a cycle opened so far.
 */
public record GetPeriodsQuery(UUID cycleId, UUID requesterAccountId) {
}
