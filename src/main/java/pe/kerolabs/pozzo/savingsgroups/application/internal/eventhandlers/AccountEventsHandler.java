package pe.kerolabs.pozzo.savingsgroups.application.internal.eventhandlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.kerolabs.pozzo.iam.interfaces.events.AccountRegisteredIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.application.commandservices.SavingsGroupCommandService;
import pe.kerolabs.pozzo.savingsgroups.domain.model.commands.LinkManualMembersCommand;

/**
 * Anti-corruption layer: when someone signs up, links the account to the members the organizers registered
 * without the application with that number.
 *
 * <p>It runs once the account is committed and in its own transaction, so a problem here never undoes the
 * registration.</p>
 */
@Slf4j
@Component
public class AccountEventsHandler {

    private final SavingsGroupCommandService savingsGroupCommandService;

    public AccountEventsHandler(SavingsGroupCommandService savingsGroupCommandService) {
        this.savingsGroupCommandService = savingsGroupCommandService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(AccountRegisteredIntegrationEvent event) {
        var linked = savingsGroupCommandService.handle(
                new LinkManualMembersCommand(event.accountId(), event.phoneNumber())).getOrElse(0);
        if (linked > 0) {
            log.info("Account {} linked to {} membership(s)", event.accountId(), linked);
        }
    }
}
