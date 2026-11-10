package pe.kerolabs.pozzo.notifications.domain.model.commands;

import java.util.UUID;

/**
 * Cancels the reminders still queued for a member in a group, when they leave it.
 */
public record CancelGroupRemindersCommand(UUID groupId, UUID accountId) {
}
