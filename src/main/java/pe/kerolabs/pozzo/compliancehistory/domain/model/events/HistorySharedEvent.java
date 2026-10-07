package pe.kerolabs.pozzo.compliancehistory.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member shared their history with a link. Kept for auditing.
 */
public record HistorySharedEvent(UUID accountId, Instant expiresAt, Instant occurredAt) {
}
