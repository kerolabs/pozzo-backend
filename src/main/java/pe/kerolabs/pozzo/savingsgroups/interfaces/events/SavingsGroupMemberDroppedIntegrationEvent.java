package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A member left a group in progress. Contributions records it for the cycle and Notifications tells
 * the other members and stops the reminders of the one who left.
 *
 * @param accountId the account of the member, or null for a member without the application
 */
public record SavingsGroupMemberDroppedIntegrationEvent(UUID groupId, String groupName, UUID membershipId,
                                                        @Nullable UUID accountId, String displayName,
                                                        List<UUID> otherMemberAccountIds, Instant occurredAt) {
}
