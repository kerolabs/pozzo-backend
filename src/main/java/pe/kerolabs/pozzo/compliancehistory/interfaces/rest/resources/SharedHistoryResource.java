package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * What a shared link shows to someone outside Pozzo.
 */
@Schema(name = "SharedHistory", description = "Compliance summary shown through a shared link")
public record SharedHistoryResource(
        @Schema(description = "Name of the member", example = "Sofia Gonzales") String displayName,
        @Schema(description = "Compliance summary") ComplianceSummaryResource summary,
        @Schema(description = "Moment the link stops working") Instant expiresAt) {
}
