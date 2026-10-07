package pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.events.HistoryUpdatedEvent;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.services.ComplianceScoringService;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * MemberRecord aggregate root: the compliance history of a member across every savings group.
 * Its identity is the member's account. It keeps the summary up to date with each new entry, so
 * reading it never walks through all the entries.
 */
@Getter
public class MemberRecord extends AbstractDomainAggregateRoot<MemberRecord> {

    private UUID accountId;
    private List<ComplianceEntry> entries = new ArrayList<>();
    private ComplianceSummary summary;
    private Instant updatedAt;

    public MemberRecord() {
    }

    public static MemberRecord forMember(UUID accountId, Instant now) {
        var record = new MemberRecord();
        record.accountId = accountId;
        record.summary = ComplianceSummary.empty();
        record.updatedAt = now;
        return record;
    }

    /**
     * Adds a fact and recomputes the summary. A fact already recorded is ignored.
     */
    public void record(ComplianceEntry entry, ComplianceScoringService scoring, Instant now) {
        if (entries.stream().anyMatch(existing -> existing.getSourceEventId().equals(entry.getSourceEventId()))) {
            return;
        }
        entries.add(entry);
        summary = scoring.summarize(entries);
        updatedAt = now;
        registerDomainEvent(new HistoryUpdatedEvent(accountId, entry.getKind(), now));
    }

    public List<ComplianceEntry> entriesInCycle(UUID cycleId) {
        return entries.stream().filter(entry -> entry.getCycleId().equals(cycleId)).toList();
    }

    public List<ComplianceEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID accountId, List<ComplianceEntry> entries, ComplianceSummary summary,
                             Instant updatedAt) {
        this.accountId = accountId;
        this.entries = new ArrayList<>(entries);
        this.summary = summary;
        this.updatedAt = updatedAt;
    }
}
