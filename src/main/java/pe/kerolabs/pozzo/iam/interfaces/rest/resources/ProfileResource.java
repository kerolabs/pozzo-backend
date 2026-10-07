package pe.kerolabs.pozzo.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.Theme;

import java.util.UUID;

/**
 * Profile of a member.
 */
@Schema(name = "ProfileResponse", description = "Profile of a member")
public record ProfileResource(
        @Schema(description = "Account identifier")
        UUID accountId,

        @Schema(description = "Phone number in E.164 format", example = "+51999000123")
        String phoneNumber,

        @Schema(description = "Name shown to the group", example = "Anna Weber")
        String displayName,

        @Schema(description = "Photo URL", nullable = true)
        String photoUrl,

        @Schema(description = "Visual theme", example = "SYSTEM")
        Theme theme,

        @Schema(description = "Nine digits of the Yape or Plin number", example = "999000123", nullable = true)
        String walletNumber,

        @Schema(description = "Backup email", example = "anna@ejemplo.pe", nullable = true)
        String backupEmail) {
}
