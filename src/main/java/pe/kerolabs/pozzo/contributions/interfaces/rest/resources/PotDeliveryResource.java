package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result of confirming a pot delivery: the next period that opened, or the closed cycle.
 */
@Schema(name = "PotDelivery", description = "Result of confirming a pot delivery")
public record PotDeliveryResource(
        @Schema(description = "Period whose pot was delivered") UUID deliveredPeriodId,
        @Schema(description = "Turn of that period", example = "3") int deliveredTurn,
        @Schema(description = "Amount delivered", example = "2400.00") BigDecimal deliveredAmount,
        @Schema(description = "ACTIVE, or CLOSED after the last turn") CycleStatus cycleStatus,
        @Schema(description = "Period that opened", nullable = true) UUID nextPeriodId,
        @Schema(description = "Turn of that period", nullable = true) Integer nextTurn) {
}
