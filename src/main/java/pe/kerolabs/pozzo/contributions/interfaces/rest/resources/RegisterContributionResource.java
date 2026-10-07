package pe.kerolabs.pozzo.contributions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReceiptSource;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data read from the receipt on the phone and confirmed by the member.
 */
@Schema(name = "RegisterContributionRequest", description = "Data of the receipt, read on the phone and confirmed")
public record RegisterContributionResource(
        @Schema(description = "Operation number printed on the receipt", example = "04581273")
        @NotBlank
        @Size(max = 40)
        String operationNumber,

        @Schema(description = "Who paid, if the receipt shows it", example = "Sofia Gonzales", nullable = true)
        @Size(max = 120)
        String payerName,

        @Schema(description = "Who received the money", example = "Anna Weber")
        @NotBlank
        @Size(max = 120)
        String payeeName,

        @Schema(description = "Amount paid, in soles", example = "300.00")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @Schema(description = "Date of the payment", example = "2026-12-31")
        @NotNull
        LocalDate paidAt,

        @Schema(description = "Application that issued the receipt", example = "YAPE")
        @NotNull
        ReceiptSource source) {
}
