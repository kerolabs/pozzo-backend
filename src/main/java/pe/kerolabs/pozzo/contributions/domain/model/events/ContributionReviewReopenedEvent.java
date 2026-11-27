package pe.kerolabs.pozzo.contributions.domain.model.events;

import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ReviewDecision;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer took back the approval or the rejection of a contribution; it waits for review again.
 */
public record ContributionReviewReopenedEvent(UUID contributionId, UUID cycleId, UUID periodId, UUID membershipId,
                                              UUID organizerAccountId, ReviewDecision previousDecision,
                                              Instant occurredAt) {
}
