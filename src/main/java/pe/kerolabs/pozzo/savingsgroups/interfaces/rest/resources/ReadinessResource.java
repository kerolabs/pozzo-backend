package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Conditions to start a group, as the organizer sees them before starting it.
 */
@Schema(name = "Readiness", description = "Conditions to start the group")
public record ReadinessResource(
        @Schema(description = "Every seat is taken") boolean groupFull,
        @Schema(description = "The collection order is defined") boolean turnsAssigned,
        @Schema(description = "The destination of the contributions is defined") boolean destinationDefined,
        @Schema(description = "All the conditions are met") boolean canStart) {
}
