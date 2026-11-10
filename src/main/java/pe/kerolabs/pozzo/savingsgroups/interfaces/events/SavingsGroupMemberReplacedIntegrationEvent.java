package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * Someone took the seat and the turn of a member who dropped out. Contributions gives them the turn and
 * the pending contributions, and Notifications welcomes them.
 *
 * @param accountId the account of the replacement, or null when they do not use the application
 */
public record SavingsGroupMemberReplacedIntegrationEvent(UUID groupId, String groupName, UUID droppedMembershipId,
                                                         UUID replacementMembershipId, @Nullable UUID accountId,
                                                         String displayName, int turnNumber, Instant occurredAt) {
}
