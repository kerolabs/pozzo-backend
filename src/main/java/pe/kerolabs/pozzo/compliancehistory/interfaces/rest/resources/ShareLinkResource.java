package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * A link that shows the requester's summary to someone outside Pozzo.
 */
@Schema(name = "ShareLink", description = "Link to share the compliance summary")
public record ShareLinkResource(
        @Schema(description = "Token of the link, used to revoke it") String token,
        @Schema(description = "Public URL for the share sheet") String url,
        @Schema(description = "Moment the link stops working") Instant expiresAt) {
}
