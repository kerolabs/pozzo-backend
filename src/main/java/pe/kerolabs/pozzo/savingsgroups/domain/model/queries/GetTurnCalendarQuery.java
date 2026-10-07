package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

import java.util.UUID;

/**
 * Query for the collection order of a group the requester belongs to.
 */
public record GetTurnCalendarQuery(UUID groupId, UUID requesterId) {
}
