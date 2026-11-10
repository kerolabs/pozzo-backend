package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer swapped the turns of two members whose periods had not started.
 *
 * @param firstTurn  the turn the first member collects in now
 * @param secondTurn the turn the second member collects in now
 */
public record TurnsSwappedEvent(UUID cycleId, UUID groupId, UUID firstMembershipId, int firstTurn,
                                UUID secondMembershipId, int secondTurn, Instant occurredAt) {
}
