package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * The requester's own history: the summary and the detail by group.
 */
@Schema(name = "MyHistory", description = "The requester's compliance history")
public record MyHistoryResource(
        @Schema(description = "Summary across every group") ComplianceSummaryResource summary,
        @Schema(description = "Detail by group, the most recent first") List<GroupItem> groups) {

    @Schema(name = "HistoryByGroup", description = "Compliance in one group")
    public record GroupItem(
            @Schema(description = "Group identifier") UUID groupId,
            @Schema(description = "Name of the group", example = "Junta del barrio") String groupName,
            @Schema(description = "IN_PROGRESS or COMPLETED") String status,
            @Schema(description = "Contributions made on time", example = "2") int onTime,
            @Schema(description = "Contributions on record", example = "2") int contributions) {
    }
}
