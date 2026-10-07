package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to register a member who does not use the application.
 */
@Schema(name = "AddManualMemberRequest", description = "Request to register a member without the application")
public record AddManualMemberResource(
        @Schema(description = "Name of the member", example = "Rosa Medina")
        @NotBlank
        @Size(max = 80)
        String displayName,

        @Schema(description = "Optional nine-digit mobile number", example = "999000000", nullable = true)
        @Pattern(regexp = "^9\\d{8}$", message = "must have nine digits and start with 9")
        String phoneNumber) {
}
