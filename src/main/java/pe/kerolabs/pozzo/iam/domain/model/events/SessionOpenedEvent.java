package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member signed in on a device.
 */
public record SessionOpenedEvent(UUID sessionId, UUID accountId, Instant occurredAt) {
}
