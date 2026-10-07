package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A phone number was proven to belong to the person who requested the code.
 */
public record CodeVerifiedEvent(UUID verificationCodeId, String phoneNumber, Instant occurredAt) {
}
