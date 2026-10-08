package pe.kerolabs.pozzo.compliancehistory.domain.model.queries;

import java.util.UUID;

/**
 * Query for the compliance summary of every member of a group; only for its organizer.
 */
public record GetGroupComplianceQuery(UUID groupId, UUID requesterAccountId) {
}
