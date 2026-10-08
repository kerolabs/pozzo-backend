package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member covered the contribution of another member, who now owes that amount.
 */
public record ContributionCoveredEvent(UUID contributionId, UUID cycleId, UUID periodId, UUID membershipId,
                                       UUID coveredByMembershipId, Instant occurredAt) {
}
