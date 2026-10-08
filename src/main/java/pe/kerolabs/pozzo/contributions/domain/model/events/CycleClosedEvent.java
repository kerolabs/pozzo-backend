package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Every member has collected: the cycle is over.
 */
public record CycleClosedEvent(UUID cycleId, UUID groupId, Instant occurredAt) {
}
