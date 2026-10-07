package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request to change the name and rules of a group that has not started.
 */
@Schema(name = "UpdateRulesRequest", description = "Request to change the name and rules of a group")
public record UpdateRulesResource(
        @Schema(description = "Name of the group", example = "Junta del barrio")
        @NotBlank
        @Size(max = 80)
        String name,

        @Schema(description = "Fixed amount each member contributes every period, in soles", example = "350.00")
        @NotNull
        @DecimalMin(value = "1.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal contributionAmount,

        @Schema(description = "How often the members contribute", example = "MONTHLY")
        @NotNull
        Periodicity periodicity,

        @Schema(description = "Number of members", example = "8")
        @Min(2)
        @Max(50)
        int seats,

        @Schema(description = "Cutoff date of the first period", example = "2026-11-05")
        @NotNull
        LocalDate firstContributionDate) {
}
