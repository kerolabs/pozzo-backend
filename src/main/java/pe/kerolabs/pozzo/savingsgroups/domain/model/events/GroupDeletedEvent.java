package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The organizer deleted a group that had not started. It carries what the other contexts need, because
 * the group no longer exists when they hear about it.
 *
 * @param groupId          the deleted group
 * @param groupName        the name it had
 * @param organizerId      the account of the organizer
 * @param memberAccountIds the accounts of the other members who use the application
 * @param occurredAt       the moment it was deleted
 */
public record GroupDeletedEvent(UUID groupId, String groupName, UUID organizerId, List<UUID> memberAccountIds,
                                Instant occurredAt) {
}
