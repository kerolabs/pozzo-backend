package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Every member of a period has paid or been covered: the pot can be delivered.
 */
public record PotCompletedEvent(UUID periodId, UUID cycleId, int turnNumber, Instant occurredAt) {
}
