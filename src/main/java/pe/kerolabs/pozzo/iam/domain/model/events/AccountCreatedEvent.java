package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member completed the registration and now has an account.
 */
public record AccountCreatedEvent(UUID accountId, String phoneNumber, String displayName, Instant occurredAt) {
}
