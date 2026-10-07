package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.CycleResource;

import java.util.UUID;

/**
 * Converts a {@link Cycle} into the {@link CycleResource} one of its members sees.
 */
public class CycleResourceFromEntityAssembler {

    public static CycleResource toResourceFromEntity(Cycle cycle, UUID requesterAccountId) {
        var rules = cycle.getRules();
        var me = cycle.participantOf(requesterAccountId).orElseThrow();
        return new CycleResource(
                cycle.getId(),
                cycle.getGroupId(),
                cycle.getGroupName(),
                cycle.getStatus(),
                cycle.getCurrentTurn(),
                cycle.totalTurns(),
                rules.contribution().amount(),
                rules.contribution().currency(),
                rules.periodicity(),
                rules.contribution().times(cycle.totalTurns()).amount(),
                rules.destinationMethod(),
                rules.destinationPhone().replace("+51", ""),
                rules.payeeName(),
                cycle.isOrganizer(requesterAccountId) ? "ORGANIZER" : "PARTICIPANT",
                me.membershipId(),
                me.turnNumber(),
                cycle.getStartedAt(),
                cycle.getClosedAt());
    }
}
