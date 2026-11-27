package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * What a shared link shows to someone outside Pozzo: the summary and each group, never its name or amounts.
 */
@Schema(name = "SharedHistory", description = "Compliance summary shown through a shared link")
public record SharedHistoryResource(
        @Schema(description = "Name of the member", example = "Sofia Gonzales") String displayName,
        @Schema(description = "Compliance summary") ComplianceSummaryResource summary,
        @Schema(description = "Compliance in each group, in the order the member joined them") List<GroupItem> groups,
        @Schema(description = "Moment the link stops working") Instant expiresAt) {

    @Schema(name = "SharedHistoryByGroup", description = "Compliance in one group, without the name of the group")
    public record GroupItem(
            @Schema(description = "IN_PROGRESS or COMPLETED") String status,
            @Schema(description = "Contributions made on time", example = "4") int onTime,
            @Schema(description = "Contributions made late", example = "1") int late,
            @Schema(description = "Contributions covered by another member", example = "0") int covered,
            @Schema(description = "Contributions on record", example = "5") int contributions) {
    }
}
