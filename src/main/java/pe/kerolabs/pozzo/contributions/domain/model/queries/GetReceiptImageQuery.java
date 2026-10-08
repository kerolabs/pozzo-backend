package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for a temporary link to the receipt image of a contribution, for its member or the organizer.
 */
public record GetReceiptImageQuery(UUID contributionId, UUID requesterAccountId) {
}
