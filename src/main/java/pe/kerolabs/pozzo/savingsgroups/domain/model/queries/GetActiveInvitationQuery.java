package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

import java.util.UUID;

/**
 * Query for the active invitation of a group, for its organizer.
 */
public record GetActiveInvitationQuery(UUID groupId, UUID requesterId) {
}
