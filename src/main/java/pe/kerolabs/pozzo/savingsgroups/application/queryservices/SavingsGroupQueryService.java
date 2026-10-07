package pe.kerolabs.pozzo.savingsgroups.application.queryservices;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.Invitation;
import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetActiveInvitationQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupByIdQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetGroupPreviewQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMembersQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetMyGroupsQuery;
import pe.kerolabs.pozzo.savingsgroups.domain.model.queries.GetTurnCalendarQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for savings group read queries.
 *
 * <p>The queries for one group return empty when the requester is not a member,
 * exactly as when the group does not exist.</p>
 */
public interface SavingsGroupQueryService {

    Optional<SavingsGroup> handle(GetGroupByIdQuery query);

    Optional<SavingsGroup> handle(GetMembersQuery query);

    Optional<SavingsGroup> handle(GetTurnCalendarQuery query);

    /**
     * Resolves an invitation code. Empty when the code does not exist or can no longer be used.
     */
    Optional<GroupPreview> handle(GetGroupPreviewQuery query);

    List<SavingsGroup> handle(GetMyGroupsQuery query);

    /**
     * The active invitation of a group, for its organizer only.
     */
    Optional<Invitation> handle(GetActiveInvitationQuery query);
}
