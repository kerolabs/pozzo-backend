package pe.kerolabs.pozzo.savingsgroups.interfaces.rest.transform;

import pe.kerolabs.pozzo.savingsgroups.domain.model.aggregates.SavingsGroup;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects.TurnSlot;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.GroupResource;
import pe.kerolabs.pozzo.savingsgroups.interfaces.rest.resources.ReadinessResource;

import java.util.UUID;

/**
 * Converts a {@link SavingsGroup} into the {@link GroupResource} one of its members sees.
 */
public class GroupResourceFromEntityAssembler {

    public static GroupResource toResourceFromEntity(SavingsGroup group, UUID requesterId) {
        var myTurn = group.membershipOf(requesterId).flatMap(membership -> group.turnOf(membership.getId()));
        return new GroupResource(
                group.getId(),
                group.getName(),
                group.getStatus(),
                group.isOrganizer(requesterId) ? "ORGANIZER" : "PARTICIPANT",
                organizerName(group),
                RulesResourceFromEntityAssembler.toResourceFromEntity(group.getRules(), true),
                group.activeMembersCount(),
                group.freeSeats(),
                group.getTurnMethod(),
                myTurn.map(TurnSlot::turnNumber).orElse(null),
                myTurn.map(turn -> group.cutoffDateOfTurn(turn.turnNumber())).orElse(null),
                new ReadinessResource(group.isFull(), group.hasTurns(), group.getRules().hasDestination(),
                        group.canStart()),
                group.getCreatedAt(),
                group.getStartedAt());
    }

    static String organizerName(SavingsGroup group) {
        return group.membershipOf(group.getOrganizerId()).map(Membership::getDisplayName).orElse("");
    }
}
