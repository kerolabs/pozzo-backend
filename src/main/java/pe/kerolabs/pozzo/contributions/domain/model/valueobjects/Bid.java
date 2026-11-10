package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import java.time.Instant;
import java.util.UUID;

/**
 * The discount on the pot a member offers to collect in a turn.
 */
public record Bid(UUID membershipId, Money amount, Instant placedAt) {
}
