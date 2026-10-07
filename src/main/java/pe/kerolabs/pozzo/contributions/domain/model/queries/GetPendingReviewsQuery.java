package pe.kerolabs.pozzo.contributions.domain.model.queries;

import java.util.UUID;

/**
 * Query for the contributions of a period that wait for the organizer's review.
 */
public record GetPendingReviewsQuery(UUID periodId, UUID requesterAccountId) {
}
