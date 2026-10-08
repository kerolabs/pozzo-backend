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
     * Retrieves the requester's own compliance history; returns an empty record when nothing has been recorded yet.
     *
     * @param query query with member's account ID
     * @return the member's record aggregate
     */
    MemberRecord handle(GetMyHistoryQuery query);

    /**
     * Retrieves the compliance summary of every active member in a group, accessible only by the group organizer.
     *
     * @param query query containing group ID and organizer account ID
     * @return optional list of member compliance summaries, or empty if unauthorized
     */
    Optional<List<MemberCompliance>> handle(GetGroupComplianceQuery query);

    /**
     * The summary of a member, for the member or one of their organizers; empty for anyone else.
     *
     * @param query query containing target member ID and requester account ID
     * @return optional compliance summary, or empty if not accessible
     */
    Optional<ComplianceSummary> handle(GetMemberSummaryQuery query);

    /**
     * What a shared link shows; empty when the token is malformed, unknown, revoked or expired.
     *
     * @param query query containing public share token
     * @return optional shared history details, or empty if token expired/invalid
     */
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
