package pe.kerolabs.pozzo.compliancehistory.domain.model.commands;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to record a compliance fact; the event handlers issue it from the events of other contexts.
 *
 * @param sourceEventId identifies the fact, so it is recorded only once
 */
public record RecordComplianceEntryCommand(UUID accountId, UUID cycleId, UUID groupId, String groupName,
                                           @Nullable UUID periodId, EntryKind kind, Instant occurredAt,
                                           String sourceEventId) {
}
