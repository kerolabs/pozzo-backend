package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The public link that proves a contribution was registered.
 */
@Schema(name = "ContributionShareLink", description = "Public link that proves a contribution was registered")
public record ContributionShareLinkResource(
        @Schema(description = "Token of the link") String token,
        @Schema(description = "Public URL for the share sheet",
                example = "https://kerolabs.github.io/aporte/?t=q3Vb8sZk1xY2mN0pR4tW7uAb") String url) {
}
