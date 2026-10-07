package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PeriodStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * State of the pot of a period and of every member's contribution.
 */
@Schema(name = "PeriodStatus", description = "State of the pot of a period and of every contribution")
public record PeriodStatusResource(
        @Schema(description = "Period identifier") UUID periodId,
        @Schema(description = "Cycle identifier") UUID cycleId,
        @Schema(description = "Name of the group", example = "Junta del barrio") String groupName,
        @Schema(description = "Turn of the period", example = "3") int turnNumber,
        @Schema(description = "Number of turns of the cycle", example = "8") int totalTurns,
        @Schema(description = "OPEN, POT_COMPLETE or DELIVERED") PeriodStatus status,
        @Schema(description = "First day of the period") LocalDate opensAt,
        @Schema(description = "Last day to contribute") LocalDate cutoffDate,
        @Schema(description = "Days left until the cutoff; negative once it has passed", example = "5") long daysToCutoff,
        @Schema(description = "Membership of the member who collects") UUID payoutMembershipId,
        @Schema(description = "Name of the member who collects", example = "Carla Vega") String payoutMemberName,
        @Schema(description = "Contribution per member", example = "300.00") BigDecimal contributionAmount,
        @Schema(description = "Full pot", example = "2400.00") BigDecimal potAmount,
        @Schema(description = "Gathered so far", example = "1200.00") BigDecimal collectedAmount,
        @Schema(description = "Still missing", example = "1200.00") BigDecimal missingAmount,
        @Schema(description = "Members who already settled", example = "4") int settledCount,
        @Schema(description = "Members of the cycle", example = "8") int membersCount,
        @Schema(description = "State of the requester's contribution") String myStatus,
        @Schema(description = "When the pot was delivered", nullable = true) Instant deliveredAt,
        @Schema(description = "Every member with the state of their contribution") List<MemberStatus> members) {

    /**
     * State of one member's contribution in the period.
     */
    @Schema(name = "MemberContributionStatus", description = "State of one member's contribution")
    public record MemberStatus(
            @Schema(description = "Membership identifier") UUID membershipId,
            @Schema(description = "Name of the member", example = "Marta Quispe") String displayName,
            @Schema(description = "True for the requester") boolean me,
            @Schema(description = "True for the member who collects in this period") boolean collects,
            @Schema(description = "VALIDATED, IN_REVIEW, PENDING, LATE or COVERED", example = "VALIDATED") String status,
            @Schema(description = "The contribution behind the state, if any", nullable = true) UUID contributionId) {
    }
}
