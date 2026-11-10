package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member who took part without the application linked the account they signed up with to their turn.
 */
public record CycleMemberLinkedEvent(UUID cycleId, UUID groupId, UUID membershipId, UUID accountId,
                                     Instant occurredAt) {
}
