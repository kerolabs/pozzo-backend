package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceLevel;

/**
 * Compliance summary of a member: never amounts or names of other groups.
 */
@Schema(name = "ComplianceSummary", description = "Compliance summary of a member")
public record ComplianceSummaryResource(
        @Schema(description = "EXCELLENT, GOOD, REGULAR, RISKY or NEW", example = "GOOD") ComplianceLevel level,
        @Schema(description = "Percentage of contributions made on time; null without contributions",
                example = "93", nullable = true) Integer complianceRate,
        @Schema(description = "Contributions on record", example = "15") int contributions,
        @Schema(description = "Made on time", example = "14") int onTime,
        @Schema(description = "Made late", example = "1") int late,
        @Schema(description = "Covered by another member", example = "0") int covered,
        @Schema(description = "Receipts rejected by an organizer", example = "0") int rejected,
        @Schema(description = "Groups left after they started", example = "0") int dropouts,
        @Schema(description = "Cycles completed", example = "1") int cyclesCompleted) {
}
