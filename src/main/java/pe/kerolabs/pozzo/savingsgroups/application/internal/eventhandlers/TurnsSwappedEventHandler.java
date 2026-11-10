package pe.kerolabs.pozzo.savingsgroups.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.interfaces.events.TurnsSwappedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.ApplySwappedTurnsCommand;

/**
 * Anti-corruption layer: keeps the calendar of the group in step when Contributions swaps two turns.
 * It runs in the same transaction as the swap, so the calendar and the cycle never disagree.
 */
@Component
public class TurnsSwappedEventHandler {

    private final SavingsGroupCommandService savingsGroupCommandService;

    public TurnsSwappedEventHandler(SavingsGroupCommandService savingsGroupCommandService) {
        this.savingsGroupCommandService = savingsGroupCommandService;
    }

    @EventListener
    public void on(TurnsSwappedIntegrationEvent event) {
        var first = event.swapped().getFirst();
        var second = event.swapped().getLast();
        var result = savingsGroupCommandService.handle(new ApplySwappedTurnsCommand(event.groupId(),
                first.member().membershipId(), first.turnNumber(), second.member().membershipId(),
                second.turnNumber()));
        if (result.isFailure()) {
            throw new IllegalStateException("The calendar of group %s could not follow the swap"
                    .formatted(event.groupId()));
        }
    }
}
