package pe.kerolabs.pozzo.compliancehistory.domain.model.queries;

import java.util.UUID;

/**
 * Query for the requester's own history, with the detail by group.
 */
public record GetMyHistoryQuery(UUID accountId) {
}
