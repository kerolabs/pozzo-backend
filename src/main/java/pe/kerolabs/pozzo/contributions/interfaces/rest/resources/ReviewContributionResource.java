package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;

/**
 * Decision of the organizer about a contribution whose receipt did not match.
 */
@Schema(name = "ReviewContributionRequest", description = "Decision about a contribution under review")
public record ReviewContributionResource(
        @Schema(description = "Approve or reject", example = "APPROVE")
        @NotNull
        ReviewDecision decision,

        @Schema(description = "Optional note for the member", nullable = true)
        @Size(max = 300)
        String note) {
}
