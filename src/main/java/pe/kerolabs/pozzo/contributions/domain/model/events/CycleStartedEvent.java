package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A cycle started for a savings group and its first period opened.
 */
public record CycleStartedEvent(UUID cycleId, UUID groupId, Instant occurredAt) {
}
