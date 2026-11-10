package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The organizer opened the next cycle of a group with the same rules and members.
 * Notifications tells the members who have the application.
 */
public record SavingsGroupNewCycleOpenedIntegrationEvent(UUID groupId, UUID previousGroupId, String groupName,
                                                         List<UUID> memberAccountIds, Instant occurredAt) {
}
