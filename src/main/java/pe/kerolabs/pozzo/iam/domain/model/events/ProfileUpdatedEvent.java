package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member changed the display name, photo or theme.
 */
public record ProfileUpdatedEvent(UUID accountId, String displayName, Instant occurredAt) {
}
