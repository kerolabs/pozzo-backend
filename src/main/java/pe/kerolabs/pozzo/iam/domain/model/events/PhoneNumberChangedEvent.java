package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * The account is now identified by another phone number; its groups and history stay the same.
 *
 * @param recovered true when the change came from the recovery by backup email
 */
public record PhoneNumberChangedEvent(UUID accountId, String phoneNumber, boolean recovered, Instant occurredAt) {
}
