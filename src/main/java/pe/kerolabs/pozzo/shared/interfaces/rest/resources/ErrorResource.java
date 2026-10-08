package pe.kerolabs.pozzo.shared.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

/**
 * Standard error body returned by every REST endpoint.
 */
@Schema(name = "Error", description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResource(
        @Schema(description = "Machine-readable error code", example = "SAVINGS_GROUP_NOT_FOUND")
        String code,

        @Schema(description = "Localized human-readable message", example = "No se encontró el recurso")
        String message,

        @Schema(description = "Additional context about the error", nullable = true)
        @Nullable String details) {

    public ErrorResource(String code, String message) {
        this(code, message, null);
    }
}
