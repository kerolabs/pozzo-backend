package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * What a public verification link shows of a contribution: enough to prove it, without the receipt image or
 * the full operation number.
 */
@Schema(name = "VerifiedContribution", description = "A contribution registered in Pozzo, as a shared link shows it")
public record VerifiedContributionResource(
        @Schema(description = "Name of the group", example = "Junta de la familia Weber") String groupName,
        @Schema(description = "Member the contribution counts for", example = "Sofia Gonzales") String memberName,
        @Schema(description = "Turn of the period", example = "3") int turnNumber,
        @Schema(description = "Cutoff date of the period") LocalDate cutoffDate,
        @Schema(description = "Amount", example = "200.00") BigDecimal amount,
        @Schema(description = "Currency", example = "PEN") String currency,
        @Schema(description = "TRANSFER, CASH or COVERAGE") ContributionMethod method,
        @Schema(description = "Date of the transfer, when it was a transfer", nullable = true) LocalDate paidOn,
        @Schema(description = "Last four digits of the operation number, when it was a transfer",
                example = "1273", nullable = true) String operationNumberEnding,
        @Schema(description = "When it was registered in Pozzo") Instant registeredAt) {
}
