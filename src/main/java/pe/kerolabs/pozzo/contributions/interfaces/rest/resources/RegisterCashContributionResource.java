package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

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

        @Schema(description = "Amount received, in soles. An amount other than what the member owes needs "
                + "differenceConfirmed", example = "300.00")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Schema(description = "Day the cash was received", example = "2026-12-31")
        @NotNull
        LocalDate receivedOn,

        @Schema(description = "True once the organizer confirmed an amount other than what the member owes",
                example = "false")
        @Nullable
        Boolean differenceConfirmed) {
}
