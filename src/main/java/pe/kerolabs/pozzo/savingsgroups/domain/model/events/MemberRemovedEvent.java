package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer removed a member before the group started.
 */
public record MemberRemovedEvent(UUID groupId, UUID membershipId, Instant occurredAt) {
}
