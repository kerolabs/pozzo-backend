package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

import java.util.UUID;

/**
 * Query for the members of a group the requester belongs to.
 */
public record GetMembersQuery(UUID groupId, UUID requesterId) {
}
