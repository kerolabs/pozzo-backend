package pe.kerolabs.pozzo.notifications.domain.model.commands;

import java.util.UUID;

/**
 * Command to cancel the pending reminders of a member for a period, once the member contributed.
 */
public record CancelRemindersCommand(UUID periodId, UUID accountId) {
}
