package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A receipt did not match what was expected; the organizer has to review it.
 */
public record InconsistencyDetectedEvent(UUID contributionId, UUID cycleId, UUID periodId, UUID membershipId, Instant occurredAt) {
}
