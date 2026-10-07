package pe.kerolabs.pozzo.notifications.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationContent;

import java.util.UUID;

/**
 * Command to send an alert right away. An alert with a deduplication key already used is ignored.
 */
public record SendAlertCommand(UUID accountId, UUID groupId, @Nullable UUID periodId, NotificationContent content,
                               String dedupKey) {
}
