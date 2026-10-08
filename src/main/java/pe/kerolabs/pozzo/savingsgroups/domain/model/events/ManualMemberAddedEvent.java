package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer registered a member who does not use the application.
 */
public record ManualMemberAddedEvent(UUID groupId, UUID membershipId, String displayName, Instant occurredAt) {
}
