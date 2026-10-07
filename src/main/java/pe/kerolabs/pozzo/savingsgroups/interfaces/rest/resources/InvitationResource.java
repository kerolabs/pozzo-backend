package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Invitation the organizer shares with the group.
 */
@Schema(name = "Invitation", description = "Invitation code and link of a savings group")
public record InvitationResource(
        @Schema(description = "Code to type in the application", example = "JB-7K4M") String code,
        @Schema(description = "Link that opens the application on the join screen",
                example = "https://pozzo.app/unirme/JB7K4M") String link,
        @Schema(description = "Moment the invitation expires") Instant expiresAt) {
}
