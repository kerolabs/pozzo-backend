package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A member of the cycle and the turn in which that member collects. Members are identified by
 * their membership in the savings group, because members without the application have no account.
 *
 * @param turnNumber   the position in the collection order, starting at 1
 * @param membershipId the membership in the savings group
 * @param accountId    the account of the member, or null for a member without the application
 * @param displayName  the name of the member
 */
public record CycleTurn(int turnNumber, UUID membershipId, @Nullable UUID accountId, String displayName) {

    public CycleTurn {
        if (turnNumber < 1 || membershipId == null || displayName == null) {
            throw new IllegalArgumentException("A turn needs a number from 1, a membership and a name");
        }
    }

    public boolean belongsTo(UUID account) {
        return accountId != null && accountId.equals(account);
    }
}
