package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member of the cycle dropped out.
 */
public record CycleMemberDroppedEvent(UUID cycleId, UUID groupId, UUID membershipId, int turnNumber, Instant occurredAt) {
}
