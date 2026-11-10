package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * A member left a group in progress. Their turn waits for a replacement.
 *
 * @param memberId the account of the member, or null for a member without the application
 */
public record MemberDroppedEvent(UUID groupId, UUID membershipId, @Nullable UUID memberId, String displayName, Instant occurredAt) {
}
