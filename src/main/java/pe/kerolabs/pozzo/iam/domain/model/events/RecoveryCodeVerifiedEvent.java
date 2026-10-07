package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The member typed the right recovery code: the account may be linked to a new phone number.
 */
public record RecoveryCodeVerifiedEvent(UUID recoveryCodeId, UUID accountId, Instant occurredAt) {
}
