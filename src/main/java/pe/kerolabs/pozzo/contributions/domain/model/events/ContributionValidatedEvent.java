package pe.kerolabs.pozzo.contributions.domain.model.events;

import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ContributionMethod;

import java.time.Instant;
import java.util.UUID;

/**
 * A contribution settled what a member owed in a period: a matching receipt, an approved one, or cash.
 * Compliance History records it; {@code onTime} says whether it arrived by the cutoff date.
 */
public record ContributionValidatedEvent(UUID contributionId, UUID cycleId, UUID periodId, UUID membershipId,
                                         @Nullable UUID accountId, ContributionMethod method, boolean onTime, Instant occurredAt) {
}
