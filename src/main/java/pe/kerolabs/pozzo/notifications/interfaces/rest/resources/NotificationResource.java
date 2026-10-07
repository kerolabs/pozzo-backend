package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.NotificationKind;

import java.time.Instant;
import java.util.UUID;

/**
 * A notification already sent to the member.
 */
@Schema(name = "Notification", description = "Reminder or alert sent to the member")
public record NotificationResource(UUID id, UUID groupId,
                                   @Schema(nullable = true) UUID periodId,
                                   NotificationKind kind, String title, String body,
                                   @Schema(nullable = true) String deepLink,
                                   Instant sentAt) {
}
