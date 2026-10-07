package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cash the organizer received from a member.
 */
@Schema(name = "RegisterCashContributionRequest", description = "Cash the organizer received from a member")
public record RegisterCashContributionResource(
        @Schema(description = "Membership of the member who paid in cash")
        @NotNull
        UUID membershipId,

        @Schema(description = "Amount received, in soles; it must be the contribution", example = "300.00")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Schema(description = "Day the cash was received", example = "2026-12-31")
        @NotNull
        LocalDate receivedOn) {
}
