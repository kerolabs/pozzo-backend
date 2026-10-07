package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * What a member sees before joining with an invitation code: no member list and no destination number.
 */
@Schema(name = "GroupPreview", description = "Summary of a group shown before joining it")
public record GroupPreviewResource(
        @Schema(description = "Group identifier") UUID groupId,
        @Schema(description = "Name of the group", example = "Junta del barrio") String name,
        @Schema(description = "Lifecycle state", example = "DRAFT") GroupStatus status,
        @Schema(description = "Name of the organizer", example = "Anna Weber") String organizerName,
        @Schema(description = "Rules of the group, without the destination") RulesResource rules,
        @Schema(description = "Active members", example = "6") int membersCount,
        @Schema(description = "Seats still free", example = "2") int freeSeats,
        @Schema(description = "Moment the invitation expires") Instant invitationExpiresAt) {
}
