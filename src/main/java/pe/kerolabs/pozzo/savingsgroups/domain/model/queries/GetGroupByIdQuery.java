package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

import java.util.UUID;

/**
 * Query for a group the requester belongs to.
 */
public record GetGroupByIdQuery(UUID groupId, UUID requesterId) {
}
