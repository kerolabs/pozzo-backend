package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.transform;

import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService.MemberCompliance;
import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService.SharedHistory;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.entities.ComplianceEntry;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.ComplianceSummaryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MemberComplianceResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MyHistoryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.SharedHistoryResource;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts compliance histories and summaries into their REST resources.
 */
public class ComplianceResourceAssembler {

    public static ComplianceSummaryResource toResourceFromSummary(ComplianceSummary summary) {
        return new ComplianceSummaryResource(summary.level(), summary.complianceRate(), summary.contributions(),
                summary.onTime(), summary.late(), summary.covered(), summary.rejected(), summary.dropouts(),
                summary.cyclesCompleted());
    }

    /**
     * The summary plus the detail by group; a group is COMPLETED once its cycle closed.
     */
    public static MyHistoryResource toResourceFromRecord(MemberRecord record) {
        var byGroup = record.getEntries().stream().collect(Collectors.groupingBy(ComplianceEntry::getGroupId));
        var groups = byGroup.entrySet().stream()
                .map(group -> toGroupItem(group.getKey(), group.getValue()))
                .sorted(Comparator.comparing(GroupWithDate::lastActivity).reversed())
                .map(GroupWithDate::item)
                .toList();
        return new MyHistoryResource(toResourceFromSummary(record.getSummary()), groups);
    }

    public static MemberComplianceResource toResourceFromMemberCompliance(MemberCompliance member) {
        return new MemberComplianceResource(member.membershipId(), member.displayName(), member.organizer(),
                member.accountId() != null,
                member.summary() == null ? null : toResourceFromSummary(member.summary()));
    }

    public static SharedHistoryResource toResourceFromSharedHistory(SharedHistory shared) {
        return new SharedHistoryResource(shared.displayName(), toResourceFromSummary(shared.summary()),
                shared.expiresAt());
    }

    private static GroupWithDate toGroupItem(UUID groupId, List<ComplianceEntry> entries) {
        var completed = entries.stream().anyMatch(entry -> entry.getKind() == EntryKind.CYCLE_COMPLETED);
        var onTime = (int) entries.stream().filter(entry -> entry.getKind() == EntryKind.ON_TIME).count();
        var contributions = (int) entries.stream().filter(entry -> entry.getKind().isContribution()).count();
        var lastActivity = entries.stream().map(ComplianceEntry::getOccurredAt).max(Comparator.naturalOrder())
                .orElse(Instant.EPOCH);
        var item = new MyHistoryResource.GroupItem(groupId, entries.getFirst().getGroupName(),
                completed ? "COMPLETED" : "IN_PROGRESS", onTime, contributions);
        return new GroupWithDate(item, lastActivity);
    }

    private record GroupWithDate(MyHistoryResource.GroupItem item, Instant lastActivity) {
    }
}
