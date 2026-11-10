package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * Someone took the seat and the turn of a member who dropped out.
 *
 * @param accountId the account of the replacement, or null when they do not use the application
 */
public record MemberReplacedEvent(UUID groupId, UUID droppedMembershipId, UUID replacementMembershipId,
                                  @Nullable UUID accountId, String displayName, int turnNumber,
                                  Instant occurredAt) {
}
