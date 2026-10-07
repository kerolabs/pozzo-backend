package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipKind;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.MembershipStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * A member of a savings group.
 */
@Schema(name = "Membership", description = "A member of a savings group")
public record MembershipResource(
        @Schema(description = "Membership identifier") UUID id,
        @Schema(description = "Name of the member", example = "Rosa Medina") String displayName,
        @Schema(description = "APP if the member uses Pozzo, MANUAL if registered by the organizer") MembershipKind kind,
        @Schema(description = "State of the membership") MembershipStatus status,
        @Schema(description = "True for the organizer") boolean organizer,
        @Schema(description = "True for the requester") boolean me,
        @Schema(description = "Phone of a member without the application; only the organizer sees it",
                nullable = true) String phoneNumber,
        @Schema(description = "Turn of the member, once assigned", nullable = true) Integer turnNumber,
        @Schema(description = "When the member joined") Instant joinedAt) {
}
