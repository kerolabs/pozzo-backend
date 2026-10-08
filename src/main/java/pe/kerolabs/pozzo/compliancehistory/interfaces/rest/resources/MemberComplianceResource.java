package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Compliance of one member of a group, as the organizer sees it.
 */
@Schema(name = "MemberCompliance", description = "Compliance of one member of a group")
public record MemberComplianceResource(
        @Schema(description = "Membership identifier") UUID membershipId,
        @Schema(description = "Name of the member", example = "Sofia Gonzales") String displayName,
        @Schema(description = "True for the organizer") boolean organizer,
        @Schema(description = "False for a member registered by the organizer") boolean usesApp,
        @Schema(description = "Summary; null for a member without the application", nullable = true)
        ComplianceSummaryResource summary) {
}
