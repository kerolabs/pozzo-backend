package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer rejected a contribution; the member has to register it again.
 */
public record ContributionRejectedEvent(UUID contributionId, UUID cycleId, UUID periodId, UUID membershipId, Instant occurredAt) {
}
