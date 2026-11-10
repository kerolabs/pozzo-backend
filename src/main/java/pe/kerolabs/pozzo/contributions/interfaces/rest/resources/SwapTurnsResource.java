package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Two members who agreed to collect in each other's turn.
 */
@Schema(name = "SwapTurns", description = "Two members who swap their turns")
public record SwapTurnsResource(
        @Schema(description = "Membership of the first member") @NotNull UUID firstMembershipId,
        @Schema(description = "Membership of the second member") @NotNull UUID secondMembershipId) {
}
