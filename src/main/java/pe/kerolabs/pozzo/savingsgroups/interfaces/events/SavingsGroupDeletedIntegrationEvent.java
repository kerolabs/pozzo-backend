package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration event that Savings Groups publishes when the organizer deletes a group that had not started.
 *
 * @param groupId          the deleted group
 * @param groupName        the name it had
 * @param memberAccountIds the accounts of the members who use the application, without the organizer
 * @param deletedAt        the moment it was deleted
 */
public record SavingsGroupDeletedIntegrationEvent(UUID groupId, String groupName, List<UUID> memberAccountIds,
                                                  Instant deletedAt) {
}
