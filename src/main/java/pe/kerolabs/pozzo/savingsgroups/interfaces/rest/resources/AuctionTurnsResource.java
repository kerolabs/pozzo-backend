package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.AuctionFallback;

import java.math.BigDecimal;

/**
 * The rules with which the turns of a group are auctioned.
 */
@Schema(name = "AuctionTurns", description = "Rules with which the turns of a group are auctioned")
public record AuctionTurnsResource(
        @Schema(description = "How many days before the cutoff of each period its auction closes", example = "2")
        @NotNull @Min(0) @Max(30) Integer closesDaysBeforeCutoff,

        @Schema(description = "Smallest discount on the pot a member can offer", example = "20.00")
        @NotNull @DecimalMin(value = "0.01") BigDecimal minimumBid,

        @Schema(description = "Who gets the turn when nobody bids: DRAW or ORGANIZER", example = "DRAW")
        @NotNull AuctionFallback fallback) {
}
