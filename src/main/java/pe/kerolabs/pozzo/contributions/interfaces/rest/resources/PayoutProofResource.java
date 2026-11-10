package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * The image of the transfer of a pot was kept.
 */
@Schema(name = "PayoutProof", description = "The image of the transfer of a pot was kept")
public record PayoutProofResource(
        @Schema(description = "Period identifier") UUID periodId,
        @Schema(description = "Whether the period has the image of the transfer", example = "true")
        boolean hasPayoutProof) {
}
