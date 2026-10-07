package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member created a savings group and became its organizer.
 */
public record GroupCreatedEvent(UUID groupId, UUID organizerId, String name, Instant occurredAt) {
}
