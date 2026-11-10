package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The organizer swapped the turns of two members whose periods had not started. Savings Groups updates
 * the calendar of the group and Notifications tells both members.
 *
 * @param swapped the two members with the turn each one collects in now
 */
public record TurnsSwappedIntegrationEvent(UUID cycleId, UUID groupId, String groupName, List<SwappedTurn> swapped,
                                           Instant occurredAt) {

    /**
     * A member and the turn they collect in after the swap.
     */
    public record SwappedTurn(CycleMember member, int turnNumber) {
    }
}
