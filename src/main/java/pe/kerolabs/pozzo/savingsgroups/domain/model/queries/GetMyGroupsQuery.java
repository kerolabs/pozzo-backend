package pe.kerolabs.pozzo.savingsgroups.domain.model.queries;

import java.util.UUID;

/**
 * Query for the groups a member belongs to.
 */
public record GetMyGroupsQuery(UUID memberId) {
}
