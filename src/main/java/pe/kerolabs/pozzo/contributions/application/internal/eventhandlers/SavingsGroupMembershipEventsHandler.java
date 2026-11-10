package pe.kerolabs.pozzo.contributions.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.RegisterCycleDropoutCommand;
import pe.kerolabs.pozzo.contributions.domain.model.commands.ReplaceCycleMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupMemberDroppedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupMemberReplacedIntegrationEvent;

/**
 * Anti-corruption layer: keeps the cycle in step when a member drops out or is replaced. It runs in the same
 * transaction as the change in the group, so a replacement the cycle rejects is undone in the group too.
 */
@Component
public class SavingsGroupMembershipEventsHandler {

    private final CycleCommandService cycleCommandService;

    public SavingsGroupMembershipEventsHandler(CycleCommandService cycleCommandService) {
        this.cycleCommandService = cycleCommandService;
    }

    @EventListener
    public void on(SavingsGroupMemberDroppedIntegrationEvent event) {
        cycleCommandService.handle(new RegisterCycleDropoutCommand(event.groupId(), event.membershipId()));
    }

    @EventListener
    public void on(SavingsGroupMemberReplacedIntegrationEvent event) {
        cycleCommandService.handle(new ReplaceCycleMemberCommand(event.groupId(), event.droppedMembershipId(),
                event.replacementMembershipId(), event.accountId(), event.displayName()));
    }
}
