package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration event that Savings Groups publishes when every seat of a group is taken.
 *
 * @param groupId          the savings group
 * @param groupName        the name of the group
 * @param memberAccountIds the accounts of the members who use the application, without the organizer
 * @param filledAt         the moment the last seat was taken
 */
public record SavingsGroupFilledIntegrationEvent(UUID groupId, String groupName, List<UUID> memberAccountIds,
                                                 Instant filledAt) {
}
