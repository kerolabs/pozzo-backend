package pe.kerolabs.pozzo.contributions.interfaces.events;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A member of a cycle, as the integration events of Contributions describe it.
 *
 * @param membershipId the membership in the savings group
 * @param accountId    the account, or null for a member without the application
 * @param displayName  the name of the member
 */
public record CycleMember(UUID membershipId, @Nullable UUID accountId, String displayName) {
}
