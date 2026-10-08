package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A recovery code was sent to the backup email of an account.
 */
public record RecoveryCodeRequestedEvent(UUID recoveryCodeId, UUID accountId, Instant occurredAt) {
}
