package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * The contributions of a period the organizer approved or rejected, and can still review again.
 */
public record GetReviewedContributionsQuery(UUID periodId, UUID requesterAccountId) {
}
