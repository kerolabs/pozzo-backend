package pe.kerolabs.pozzo.iam.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A person completed the registration with a verified phone number.
 * Savings Groups links it to the members the organizers registered with that number.
 *
 * @param phoneNumber the verified number in E.164 format
 */
public record AccountRegisteredIntegrationEvent(UUID accountId, String phoneNumber, String displayName,
                                                Instant occurredAt) {
}
