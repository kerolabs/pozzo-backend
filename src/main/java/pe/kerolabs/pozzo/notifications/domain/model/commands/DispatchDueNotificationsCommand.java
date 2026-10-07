package pe.kerolabs.pozzo.notifications.domain.model.commands;

/**
 * Command to send the notifications whose moment arrived, at most the given number per run.
 */
public record DispatchDueNotificationsCommand(int limit) {
}
