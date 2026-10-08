package pe.kerolabs.pozzo.notifications.domain.model.commands;

import java.util.List;
import java.util.UUID;

/**
 * Command to change the reminders of a group; only its organizer.
 */
public record ConfigureReminderPlanCommand(UUID groupId, UUID requesterAccountId, List<Integer> offsetsInDays,
                                           int sendHour, boolean enabled) {
}
