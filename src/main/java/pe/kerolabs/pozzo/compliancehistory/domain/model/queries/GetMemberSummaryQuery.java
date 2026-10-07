package pe.kerolabs.pozzo.compliancehistory.domain.model.queries;

import java.util.UUID;

/**
 * Query for the compliance summary of another member; only for an organizer of a group the member belongs to.
 */
public record GetMemberSummaryQuery(UUID memberAccountId, UUID requesterAccountId) {
}
