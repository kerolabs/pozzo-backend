package pe.kerolabs.pozzo.contributions.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.application.commandservices.CycleCommandService;
import pe.kerolabs.pozzo.contributions.domain.model.commands.LinkCycleMemberCommand;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupMemberLinkedIntegrationEvent;

/**
 * Anti-corruption layer: when a member who took part without the application links an account, gives that
 * account the turn of the member in the cycle of the group.
 */
@Component
public class SavingsGroupMemberLinkedEventHandler {

    private final CycleCommandService cycleCommandService;

    public SavingsGroupMemberLinkedEventHandler(CycleCommandService cycleCommandService) {
        this.cycleCommandService = cycleCommandService;
    }

    @EventListener
    public void on(SavingsGroupMemberLinkedIntegrationEvent event) {
        cycleCommandService.handle(new LinkCycleMemberCommand(event.groupId(), event.membershipId(),
                event.accountId()));
    }
}
