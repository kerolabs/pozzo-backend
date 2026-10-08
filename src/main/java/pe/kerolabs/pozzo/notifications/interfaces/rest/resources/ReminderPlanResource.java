package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reminders of a group.
 */
@Schema(name = "ReminderPlan", description = "Reminders of a group")
public record ReminderPlanResource(UUID groupId, List<Integer> offsetsInDays, int sendHour, boolean enabled,
                                   Instant updatedAt) {
}
