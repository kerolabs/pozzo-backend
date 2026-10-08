package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request to send a verification code by SMS.
 */
@Schema(name = "RequestCodeRequest", description = "Request to send a verification code by SMS")
public record RequestCodeResource(
        @Schema(description = "Peruvian mobile number: nine digits starting with 9", example = "999000123")
        @NotBlank
        @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
        String phoneNumber) {
}
