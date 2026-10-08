package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import java.util.UUID;

/**
 * A position in the collection order: in turn {@code turnNumber}, the member of {@code membershipId}
 * collects the pot.
 *
 * @param turnNumber   the position, starting at 1
 * @param membershipId the membership that collects in that turn
 * @param assignedBy   how the position was decided
 */
public record TurnSlot(int turnNumber, UUID membershipId, TurnMethod assignedBy) {

    public TurnSlot {
        if (turnNumber < 1) {
            throw new IllegalArgumentException("The turn number starts at 1");
        }
        if (membershipId == null || assignedBy == null) {
            throw new IllegalArgumentException("A turn needs a membership and an assignment method");
        }
    }
}
