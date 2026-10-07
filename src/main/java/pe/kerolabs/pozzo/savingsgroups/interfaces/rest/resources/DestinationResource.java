package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.PaymentMethod;

/**
 * Where the contributions are sent. Used both in requests and in responses.
 */
@Schema(name = "Destination", description = "Yape or Plin wallet where the contributions are sent")
public record DestinationResource(
        @Schema(description = "Wallet", example = "YAPE")
        @NotNull
        PaymentMethod method,

        @Schema(description = "Nine-digit mobile number of the wallet", example = "999000123")
        @NotBlank
        @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
        String phoneNumber) {
}
