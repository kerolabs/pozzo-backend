package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.Platform;

/**
 * Phone to register for push notifications.
 */
@Schema(name = "RegisterDeviceRequest", description = "Phone that receives push notifications")
public record RegisterDeviceResource(
        @Schema(description = "Firebase Cloud Messaging token of the phone")
        @NotBlank
        @Size(max = 255)
        String pushToken,

        @Schema(description = "Operating system", example = "ANDROID")
        @NotNull
        Platform platform) {
}
