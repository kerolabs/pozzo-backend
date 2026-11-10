package pe.kerolabs.pozzo.iam.application.internal.eventhandlers;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.domain.model.events.AccountCreatedEvent;
import pe.kerolabs.pozzo.iam.interfaces.events.AccountRegisteredIntegrationEvent;

/**
 * Turns the domain events of Identity &amp; Access that other contexts care about into integration events.
 */
@Component("iamIntegrationEventsPublisher")
public class IntegrationEventsPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public IntegrationEventsPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(AccountCreatedEvent event) {
        eventPublisher.publishEvent(new AccountRegisteredIntegrationEvent(event.accountId(), event.phoneNumber(),
                event.displayName(), event.occurredAt()));
    }
}
