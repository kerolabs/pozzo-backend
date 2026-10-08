package pe.kerolabs.pozzo.compliancehistory.domain.model.events;

import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;

import java.time.Instant;
import java.util.UUID;

/**
 * A new fact was added to a member's history. Kept for auditing.
 */
public record HistoryUpdatedEvent(UUID accountId, EntryKind kind, Instant occurredAt) {
}
