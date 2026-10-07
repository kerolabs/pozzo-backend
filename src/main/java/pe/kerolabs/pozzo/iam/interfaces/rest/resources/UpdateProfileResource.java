package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Theme;

/**
 * Request to change the profile of the authenticated member.
 */
@Schema(name = "UpdateProfileRequest", description = "Request to change the profile of the authenticated member")
public record UpdateProfileResource(
        @Schema(description = "Name shown to the group", example = "Anna Weber")
        @NotBlank
        @Size(max = 80)
        String displayName,

        @Schema(description = "Photo URL, or null to remove it", nullable = true)
        @Size(max = 300)
        String photoUrl,

        @Schema(description = "Visual theme", example = "DARK")
        @NotNull
        Theme theme) {
}
