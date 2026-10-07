package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The organizer confirmed that the pot of a period was handed to the member who collects.
 */
public record PotDeliveredEvent(UUID periodId, UUID cycleId, int turnNumber, UUID payoutMembershipId, Instant occurredAt) {
}
