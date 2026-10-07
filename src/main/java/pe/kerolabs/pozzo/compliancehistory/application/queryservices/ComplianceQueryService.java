package pe.kerolabs.pozzo.compliancehistory.application.queryservices;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.compliancehistory.domain.model.aggregates.MemberRecord;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetGroupComplianceQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMemberSummaryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetMyHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.queries.GetSharedHistoryQuery;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.ComplianceSummary;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service contract for reading the compliance history. Only the member sees the detail;
 * organizers and shared links get the summary.
 */
public interface ComplianceQueryService {

    /**
     * The requester's own history; an empty record when nothing was recorded yet.
     */
    MemberRecord handle(GetMyHistoryQuery query);

    /**
     * The summary of every active member of a group, for its organizer only.
     */
    Optional<List<MemberCompliance>> handle(GetGroupComplianceQuery query);

    Optional<ComplianceSummary> handle(GetMemberSummaryQuery query);

    Optional<SharedHistory> handle(GetSharedHistoryQuery query);

    /**
     * Compliance of a member of a group; the summary is null for a member without the application.
     */
    record MemberCompliance(UUID membershipId, @Nullable UUID accountId, String displayName, boolean organizer,
                            @Nullable ComplianceSummary summary) {
    }

    /**
     * What a shared link shows: the name of the member and the summary.
     */
    record SharedHistory(String displayName, ComplianceSummary summary, Instant expiresAt) {
    }
}
