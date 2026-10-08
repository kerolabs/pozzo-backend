package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member signed out of a device. Notifications consumes it to deactivate the device.
 */
public record SessionRevokedEvent(UUID sessionId, UUID accountId, Instant occurredAt) {
}
