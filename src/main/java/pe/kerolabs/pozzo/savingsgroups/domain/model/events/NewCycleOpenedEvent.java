package pe.kerolabs.pozzo.savingsgroups.domain.model.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The organizer opened the next cycle of a group whose cycle was over, with the same rules and members.
 *
 * @param memberAccountIds the members with the application, other than the organizer
 */
public record NewCycleOpenedEvent(UUID groupId, UUID previousGroupId, String groupName, List<UUID> memberAccountIds,
                                  Instant occurredAt) {
}
