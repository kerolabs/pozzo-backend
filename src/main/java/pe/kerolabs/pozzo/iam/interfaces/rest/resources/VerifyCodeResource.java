package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to verify the code received by SMS.
 */
@Schema(name = "VerifyCodeRequest", description = "Request to verify the code received by SMS")
public record VerifyCodeResource(
        @Schema(description = "Peruvian mobile number the code was sent to", example = "999000123")
        @NotBlank
        @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
        String phoneNumber,

        @Schema(description = "Six-digit code received by SMS", example = "482913")
        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "must have six digits")
        String code,

        @Schema(description = "Name of the device, shown in the list of sessions", example = "Pixel 8", nullable = true)
        @Size(max = 80)
        String deviceLabel) {
}
