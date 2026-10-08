package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Every seat of the group is taken: the organizer can assign the turns.
 */
public record GroupFilledEvent(UUID groupId, Instant occurredAt) {
}
