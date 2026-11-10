package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * When the next cycle of a group starts collecting.
 */
@Schema(name = "OpenNewCycle", description = "When the next cycle of a group starts collecting")
public record OpenNewCycleResource(
        @Schema(description = "Cutoff date of the first period of the new cycle", example = "2026-12-05")
        @NotNull
        LocalDate firstContributionDate) {
}
