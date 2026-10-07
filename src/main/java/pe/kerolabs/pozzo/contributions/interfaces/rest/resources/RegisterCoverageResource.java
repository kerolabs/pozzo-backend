package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * A member covers what another member owes in the period.
 */
@Schema(name = "RegisterCoverageRequest", description = "A member covers what another member owes")
public record RegisterCoverageResource(
        @Schema(description = "Membership of the member who has not paid")
        @NotNull
        UUID membershipId,

        @Schema(description = "Membership of the member who puts the money")
        @NotNull
        UUID coveredByMembershipId) {
}
