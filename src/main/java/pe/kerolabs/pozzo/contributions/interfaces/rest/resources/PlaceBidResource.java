package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * The discount a member offers on the pot to collect in the turn.
 */
@Schema(name = "PlaceBid", description = "Discount offered on the pot to collect in the turn")
public record PlaceBidResource(
        @Schema(description = "Discount on the pot", example = "50.00") @NotNull @DecimalMin("0.01") BigDecimal amount) {
}
