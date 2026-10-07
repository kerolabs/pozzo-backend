package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.Periodicity;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Rules of a savings group.
 */
@Schema(name = "Rules", description = "Rules of a savings group")
public record RulesResource(
        @Schema(description = "Amount each member contributes every period", example = "300.00")
        BigDecimal contributionAmount,

        @Schema(description = "ISO 4217 currency", example = "PEN")
        String currency,

        @Schema(description = "How often the members contribute", example = "MONTHLY")
        Periodicity periodicity,

        @Schema(description = "Number of members and of periods", example = "8")
        int seats,

        @Schema(description = "Cutoff date of the first period", example = "2026-11-05")
        LocalDate firstContributionDate,

        @Schema(description = "Day of the month (monthly) or of the week, 1 Monday to 7 Sunday (weekly, biweekly)",
                example = "5")
        int cutoffDay,

        @Schema(description = "Amount one member collects in a period", example = "2400.00")
        BigDecimal potAmount,

        @Schema(description = "Where the contributions are sent", nullable = true)
        DestinationResource destination) {
}
