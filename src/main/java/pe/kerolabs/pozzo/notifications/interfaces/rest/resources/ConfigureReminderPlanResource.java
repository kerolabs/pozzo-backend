package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Reminders the organizer wants for the group.
 */
@Schema(name = "ConfigureReminderPlanRequest", description = "Reminders of a group")
public record ConfigureReminderPlanResource(
        @Schema(description = "Days before each cutoff date; 0 is the cutoff day", example = "[3, 1, 0]")
        @NotEmpty
        @Size(max = 10)
        List<@NotNull @Min(0) @Max(30) Integer> offsetsInDays,

        @Schema(description = "Hour of the day the reminders go out, Lima time", example = "9")
        @NotNull
        @Min(0)
        @Max(23)
        Integer sendHour,

        @Schema(description = "False turns the reminders of the group off", example = "true")
        @NotNull
        Boolean enabled) {
}
