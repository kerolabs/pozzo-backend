package pe.kerolabs.pozzo.savingsgroups.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleClosedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.CloseGroupCommand;

/**
 * When Contributions closes the cycle of a group, the group itself is closed. It runs in the
 * transaction that delivers the last pot, so both changes are saved together.
 */
@Component
public class CycleClosedEventHandler {

    private final SavingsGroupCommandService savingsGroupCommandService;

    public CycleClosedEventHandler(SavingsGroupCommandService savingsGroupCommandService) {
        this.savingsGroupCommandService = savingsGroupCommandService;
    }

    @EventListener
    public void on(CycleClosedIntegrationEvent event) {
        savingsGroupCommandService.handle(new CloseGroupCommand(event.groupId()));
    }
}
