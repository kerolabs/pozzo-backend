package pe.kerolabs.pozzo.notifications.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.kerolabs.pozzo.notifications.domain.model.valueobjects.Platform;

import java.time.Instant;
import java.util.UUID;

/**
 * A registered phone.
 */
@Schema(name = "Device", description = "Phone that receives push notifications")
public record DeviceResource(UUID id, Platform platform, Instant registeredAt, boolean active) {
}
