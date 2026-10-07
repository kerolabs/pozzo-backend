package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The requester's contributions in a cycle, period by period.
 */
@Schema(name = "MyContributions", description = "The requester's contributions in a cycle, period by period")
public record MyContributionsResource(
        @Schema(description = "Cycle identifier") UUID cycleId,
        @Schema(description = "Name of the group", example = "Junta del barrio") String groupName,
        @Schema(description = "Total already contributed", example = "600.00") BigDecimal contributedAmount,
        @Schema(description = "Total still pending in the opened periods", example = "300.00") BigDecimal pendingAmount,
        @Schema(description = "Opened periods, the most recent first") List<PeriodItem> periods) {

    /**
     * The requester's contribution in one period.
     */
    @Schema(name = "MyPeriodContribution", description = "The requester's contribution in one period")
    public record PeriodItem(
            @Schema(description = "Period identifier") UUID periodId,
            @Schema(description = "Turn of the period", example = "3") int turnNumber,
            @Schema(description = "Last day to contribute") LocalDate cutoffDate,
            @Schema(description = "Amount owed", example = "300.00") BigDecimal amount,
            @Schema(description = "VALIDATED, IN_REVIEW, PENDING, LATE or COVERED") String status,
            @Schema(description = "The latest contribution of the period, if any", nullable = true)
            ContributionResource contribution) {
    }
}
