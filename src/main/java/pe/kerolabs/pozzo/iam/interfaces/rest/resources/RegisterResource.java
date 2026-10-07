package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to complete the registration of a verified phone number.
 */
@Schema(name = "RegisterRequest", description = "Request to complete the registration of a verified phone number")
public record RegisterResource(
        @Schema(description = "Registration token returned by the code verification")
        @NotBlank
        String registrationToken,

        @Schema(description = "Name shown to the group", example = "Anna Weber")
        @NotBlank
        @Size(max = 80)
        String displayName,

        @Schema(description = "Optional photo URL", nullable = true)
        @Size(max = 300)
        String photoUrl,

        @Schema(description = "Whether the member accepts the Terms and Conditions and the Privacy Policy", example = "true")
        boolean termsAccepted,

        @Schema(description = "Name of the device, shown in the list of sessions", example = "Pixel 8", nullable = true)
        @Size(max = 80)
        String deviceLabel) {
}
