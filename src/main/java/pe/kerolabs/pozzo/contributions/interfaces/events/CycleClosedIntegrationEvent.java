package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Every member collected: the cycle is over. Compliance History records a completed cycle for
 * each member with an account.
 */
public record CycleClosedIntegrationEvent(UUID cycleId, UUID groupId, String groupName, List<CycleMember> members,
                                          Instant occurredAt) {
}
