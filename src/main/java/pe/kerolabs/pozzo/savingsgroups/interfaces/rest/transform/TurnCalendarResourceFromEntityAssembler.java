package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.TurnCalendarResource;

import java.util.UUID;

/**
 * Converts the turns of a {@link SavingsGroup} into {@link TurnCalendarResource}.
 */
public class TurnCalendarResourceFromEntityAssembler {

    public static TurnCalendarResource toResourceFromEntity(SavingsGroup group, UUID requesterId) {
        var turns = group.turnCalendar().stream()
                .map(turn -> {
                    var membership = group.findMembership(turn.membershipId()).orElseThrow();
                    return new TurnCalendarResource.Turn(
                            turn.turnNumber(),
                            membership.getId(),
                            membership.getDisplayName(),
                            group.cutoffDateOfTurn(turn.turnNumber()),
                            membership.belongsTo(requesterId));
                })
                .toList();
        return new TurnCalendarResource(
                group.getTurnMethod(),
                group.getDrawSeed(),
                group.getTurnsAssignedAt(),
                group.getRules().pot().amount(),
                turns);
    }
}
