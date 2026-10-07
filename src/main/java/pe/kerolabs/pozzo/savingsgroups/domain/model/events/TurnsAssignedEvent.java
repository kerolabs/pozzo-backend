package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;

import java.time.Instant;
import java.util.UUID;

/**
 * The collection order of the group was decided.
 */
public record TurnsAssignedEvent(UUID groupId, TurnMethod method, Instant occurredAt) {
}
