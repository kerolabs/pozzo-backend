package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.GroupStatus;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A savings group as one of its members sees it.
 */
@Schema(name = "Group", description = "A savings group as seen by one of its members")
public record GroupResource(
        @Schema(description = "Group identifier") UUID id,
        @Schema(description = "Name of the group", example = "Junta del barrio") String name,
        @Schema(description = "Lifecycle state", example = "DRAFT") GroupStatus status,
        @Schema(description = "Role of the requester in the group", example = "ORGANIZER",
                allowableValues = {"ORGANIZER", "PARTICIPANT"}) String role,
        @Schema(description = "Name of the organizer", example = "Anna Weber") String organizerName,
        @Schema(description = "Rules of the group") RulesResource rules,
        @Schema(description = "Active members", example = "6") int membersCount,
        @Schema(description = "Seats still free", example = "2") int freeSeats,
        @Schema(description = "How the collection order was decided", nullable = true) TurnMethod turnMethod,
        @Schema(description = "Turn of the requester, once the turns are assigned", nullable = true) Integer myTurnNumber,
        @Schema(description = "Cutoff date of the requester's turn", nullable = true) LocalDate myTurnDate,
        @Schema(description = "Conditions to start the group") ReadinessResource readiness,
        @Schema(description = "Creation date") Instant createdAt,
        @Schema(description = "Start date", nullable = true) Instant startedAt) {
}
