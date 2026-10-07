package pe.kerolabs.pozzo.notifications.domain.model.queries;

import java.util.UUID;

/**
 * Query for the reminders of a group; only its members.
 */
public record GetReminderPlanQuery(UUID groupId, UUID requesterAccountId) {
}
