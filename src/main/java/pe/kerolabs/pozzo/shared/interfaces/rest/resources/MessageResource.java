package pe.kerolabs.pozzo.shared.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Body for simple success or informational REST responses.
 */
@Schema(name = "Message", description = "Informational response")
public record MessageResource(String message) {
}
