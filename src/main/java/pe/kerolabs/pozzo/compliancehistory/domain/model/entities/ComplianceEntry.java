package pe.kerolabs.pozzo.compliancehistory.domain.model.entities;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;

import java.time.Instant;
import java.util.UUID;

/**
 * One compliance fact of a member. It keeps the id of the event that produced it, so the same event
 * is never counted twice even if it is published again.
 */
@Getter
public class ComplianceEntry {

    private UUID id;
    private UUID cycleId;
    private UUID groupId;
    private String groupName;
    private @Nullable UUID periodId;
    private EntryKind kind;
    private Instant occurredAt;
    private String sourceEventId;

    public ComplianceEntry() {
    }

    public static ComplianceEntry of(UUID cycleId, UUID groupId, String groupName, @Nullable UUID periodId,
                                     EntryKind kind, Instant occurredAt, String sourceEventId) {
        var entry = new ComplianceEntry();
        entry.id = UUID.randomUUID();
        entry.cycleId = cycleId;
        entry.groupId = groupId;
        entry.groupName = groupName;
        entry.periodId = periodId;
        entry.kind = kind;
        entry.occurredAt = occurredAt;
        entry.sourceEventId = sourceEventId;
        return entry;
    }

    public boolean isNegative() {
        return kind == EntryKind.LATE || kind == EntryKind.COVERED || kind == EntryKind.REJECTED
                || kind == EntryKind.DROPOUT;
    }

    /**
     * Restores the entity from persistence.
     */
    public void restoreState(UUID id, UUID cycleId, UUID groupId, String groupName, @Nullable UUID periodId,
                             EntryKind kind, Instant occurredAt, String sourceEventId) {
        this.id = id;
        this.cycleId = cycleId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.periodId = periodId;
        this.kind = kind;
        this.occurredAt = occurredAt;
        this.sourceEventId = sourceEventId;
    }
}
