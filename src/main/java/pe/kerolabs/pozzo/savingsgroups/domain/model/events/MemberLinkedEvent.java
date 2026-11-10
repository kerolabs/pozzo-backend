package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member the organizer registered without the application signed up with the same number and now uses
 * the account in the group.
 */
public record MemberLinkedEvent(UUID groupId, UUID membershipId, UUID accountId, Instant occurredAt) {
}
