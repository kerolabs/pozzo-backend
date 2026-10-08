package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The cycle of a started savings group, as one of its members sees it.
 */
@Schema(name = "Cycle", description = "The cycle of a started savings group")
public record CycleResource(
        @Schema(description = "Cycle identifier") UUID id,
        @Schema(description = "Savings group identifier") UUID groupId,
        @Schema(description = "Name of the group", example = "Junta del barrio") String groupName,
        @Schema(description = "ACTIVE or CLOSED") CycleStatus status,
        @Schema(description = "Turn in progress", example = "3") int currentTurn,
        @Schema(description = "Number of turns", example = "8") int totalTurns,
        @Schema(description = "Contribution per member", example = "300.00") BigDecimal contributionAmount,
        @Schema(description = "Currency", example = "PEN") String currency,
        @Schema(description = "How often the members contribute") Periodicity periodicity,
        @Schema(description = "Pot collected in each turn", example = "2400.00") BigDecimal potAmount,
        @Schema(description = "Wallet where the contributions are sent", example = "YAPE") String destinationMethod,
        @Schema(description = "Nine-digit number of the wallet", example = "999000123") String destinationPhoneNumber,
        @Schema(description = "Name a valid receipt shows as recipient", example = "Anna Weber") String payeeName,
        @Schema(description = "ORGANIZER or PARTICIPANT") String role,
        @Schema(description = "Membership of the requester") UUID myMembershipId,
        @Schema(description = "Turn of the requester", example = "4") int myTurnNumber,
        @Schema(description = "When the cycle started") Instant startedAt,
        @Schema(description = "When the cycle closed", nullable = true) Instant closedAt) {
}
