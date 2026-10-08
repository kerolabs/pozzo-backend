package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer generated a new invitation code for the group.
 */
public record InvitationGeneratedEvent(UUID invitationId, UUID groupId, String code, Instant occurredAt) {
}
