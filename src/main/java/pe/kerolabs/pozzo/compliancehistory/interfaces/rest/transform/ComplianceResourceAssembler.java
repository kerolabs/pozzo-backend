package pe.kerolabs.pozzo.compliancehistory.interfaces.rest.transform;

import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService.MemberCompliance;
import pe.kerolabs.pozzo.compliancehistory.application.queryservices.ComplianceQueryService.SharedHistory;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.GroupCompliance;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.ComplianceSummaryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MemberComplianceResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.MyHistoryResource;
import pe.kerolabs.pozzo.compliancehistory.interfaces.rest.resources.SharedHistoryResource;

import java.util.Comparator;

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
        var groups = record.groups().stream()
                .sorted(Comparator.comparing(GroupCompliance::lastActivity).reversed())
                .map(group -> new MyHistoryResource.GroupItem(group.groupId(), group.groupName(), statusOf(group),
                        group.onTime(), group.contributions()))
                .toList();
        return new MyHistoryResource(toResourceFromSummary(record.getSummary()), groups);
    }

    public static MemberComplianceResource toResourceFromMemberCompliance(MemberCompliance member) {
        return new MemberComplianceResource(member.membershipId(), member.displayName(), member.organizer(),
                member.accountId() != null,
                member.summary() == null ? null : toResourceFromSummary(member.summary()));
    }

    /**
     * The summary plus each group in the order the member joined it, without its name or identifier.
     */
    public static SharedHistoryResource toResourceFromSharedHistory(SharedHistory shared) {
        var groups = shared.groups().stream()
                .sorted(Comparator.comparing(GroupCompliance::firstActivity))
                .map(group -> new SharedHistoryResource.GroupItem(statusOf(group), group.onTime(), group.late(),
                        group.covered(), group.contributions()))
                .toList();
        return new SharedHistoryResource(shared.displayName(), toResourceFromSummary(shared.summary()), groups,
                shared.expiresAt());
    }

    private static String statusOf(GroupCompliance group) {
        return group.completed() ? "COMPLETED" : "IN_PROGRESS";
    }
}
