package pe.kerolabs.pozzo.savingsgroups.domain.services;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;

import java.util.List;
import java.util.UUID;

/**
 * Domain service contract for constructing the turn collection sequence of a savings group.
 * Supports both pseudo-random reproducible draws and consensual custom orderings.
 * Once generated, the group accepts the collection sequence via {@link SavingsGroup#assignTurns}.
 */
public interface TurnAssignmentService {

    /**
     * Orders the active members at random. The same seed always gives the same order, so any member
     * can reproduce the draw with the seed that is shown to the group.
     *
     * @param group the group whose members are drawn
     * @param seed  the seed of the draw
     * @return one turn per active member
     */
    List<TurnSlot> drawTurns(SavingsGroup group, String seed);

    /**
     * Turns the order agreed by the group into turns: the first membership collects first.
     *
     * @param group the group
     * @param order the memberships in collection order
     * @return one turn per membership in the order
     */
    List<TurnSlot> agreedTurns(SavingsGroup group, List<UUID> order);
}
