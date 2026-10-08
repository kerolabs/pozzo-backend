package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer changed the name, the rules or the destination of a group that has not started.
 */
public record RulesUpdatedEvent(UUID groupId, Instant occurredAt) {
}
