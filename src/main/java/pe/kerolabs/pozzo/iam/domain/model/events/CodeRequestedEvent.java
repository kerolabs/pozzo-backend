package pe.kerolabs.pozzo.iam.domain.model.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A verification code was issued for a phone number.
 */
public record CodeRequestedEvent(UUID verificationCodeId, String phoneNumber, Instant occurredAt) {
}
