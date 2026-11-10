package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A member the organizer registered without the application signed up with the same number and now uses
 * the account in the group. Contributions assigns the account to the turn of that membership and hands
 * the contributions recorded so far to Compliance History.
 */
public record SavingsGroupMemberLinkedIntegrationEvent(UUID groupId, UUID membershipId, UUID accountId,
                                                       Instant occurredAt) {
}
