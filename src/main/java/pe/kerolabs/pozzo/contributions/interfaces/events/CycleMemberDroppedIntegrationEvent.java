package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member of a cycle dropped out. Compliance History records it in their history.
 */
public record CycleMemberDroppedIntegrationEvent(UUID cycleId, UUID groupId, String groupName, CycleMember member,
                                                 int turnNumber, Instant occurredAt) {
}
