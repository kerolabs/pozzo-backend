package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reminders of a group.
 */
@Schema(name = "ReminderPlan", description = "Reminders of a group")
public record ReminderPlanResource(
        UUID groupId,
        @Schema(description = "Days before the cutoff a member who has not paid gets a reminder; 0 is the cutoff day",
                example = "[3, 1, 0]") List<Integer> offsetsInDays,
        @Schema(description = "Hour of the day the reminders go out, in the time zone of the group", example = "9")
        int sendHour,
        boolean enabled,
        @Schema(description = "Days after the cutoff a member who still has not paid gets one reminder a day",
                example = "30") int overdueDays,
        Instant updatedAt) {
}
