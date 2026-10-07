package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member who uses the application joined the group with an invitation code.
 */
public record MemberJoinedEvent(UUID groupId, UUID membershipId, UUID memberId, Instant occurredAt) {
}
