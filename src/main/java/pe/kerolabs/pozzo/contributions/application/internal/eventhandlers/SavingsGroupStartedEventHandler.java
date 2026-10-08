package pe.kerolabs.pozzo.contributions.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.StartCycleCommand;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupStartedIntegrationEvent;

/**
 * The only entry point of a cycle: when Savings Groups announces that a group started, it starts
 * the cycle with a copy of the rules and the turns. It runs in the transaction that starts the group,
 * so the group never ends up started without its cycle.
 */
@Slf4j
@Component
public class SavingsGroupStartedEventHandler {

    private final CycleCommandService cycleCommandService;

    public SavingsGroupStartedEventHandler(CycleCommandService cycleCommandService) {
        this.cycleCommandService = cycleCommandService;
    }

    @EventListener
    public void on(SavingsGroupStartedIntegrationEvent event) {
        var turns = event.turns().stream()
                .map(turn -> new CycleTurn(turn.turnNumber(), turn.membershipId(), turn.accountId(), turn.displayName()))
                .toList();
        var command = new StartCycleCommand(event.groupId(), event.groupName(), event.organizerAccountId(),
                event.organizerName(), event.contributionAmount(), event.currency(),
                Periodicity.valueOf(event.periodicity()), event.firstContributionDate(), event.destinationMethod(),
                event.destinationPhone(), turns);
        var result = cycleCommandService.handle(command);
        if (result.isFailure()) {
            throw new IllegalStateException("The cycle of group %s could not start".formatted(event.groupId()));
        }
        log.info("Cycle started for group {}", event.groupId());
    }
}
