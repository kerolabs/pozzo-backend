package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The collection order of a savings group, with the cutoff date of each turn.
 */
@Schema(name = "TurnCalendar", description = "Collection order of a savings group")
public record TurnCalendarResource(
        @Schema(description = "How the order was decided", nullable = true) TurnMethod method,
        @Schema(description = "Seed of the draw, so any member can reproduce it", nullable = true) String drawSeed,
        @Schema(description = "When the order was decided", nullable = true) Instant assignedAt,
        @Schema(description = "Amount collected in each turn", example = "2400.00") BigDecimal potAmount,
        @Schema(description = "Turns in collection order; empty until assigned") List<Turn> turns) {

    /**
     * One turn of the calendar.
     */
    @Schema(name = "Turn", description = "One turn of the collection order")
    public record Turn(
            @Schema(description = "Position, starting at 1", example = "1") int turnNumber,
            @Schema(description = "Membership that collects") UUID membershipId,
            @Schema(description = "Name of the member", example = "Rosa Medina") String displayName,
            @Schema(description = "Cutoff date of the period", example = "2026-11-05") LocalDate cutoffDate,
            @Schema(description = "True for the requester's turn") boolean me,
            @Schema(description = "Profile photo of the member who collects", nullable = true) String photoUrl) {
    }
}
