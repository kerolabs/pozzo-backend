package pe.kerolabs.pozzo.savingsgroups.application.internal.eventhandlers;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupStartedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupStartedIntegrationEvent;

/**
 * Translates the internal {@link GroupStartedEvent} into a {@link SavingsGroupStartedIntegrationEvent}
 * for the other contexts. It runs in the transaction that starts the group, so the cycle that
 * Contributions opens is saved together with the start, or not at all.
 */
@Component
public class GroupStartedEventHandler {

    private final SavingsGroupRepository savingsGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    public GroupStartedEventHandler(SavingsGroupRepository savingsGroupRepository,
                                    ApplicationEventPublisher eventPublisher) {
        this.savingsGroupRepository = savingsGroupRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(GroupStartedEvent event) {
        var group = savingsGroupRepository.findById(event.groupId()).orElseThrow();
        var organizerName = group.membershipOf(event.organizerId())
                .map(membership -> membership.getDisplayName())
                .orElse("");
        var rules = event.rules();
        var turns = event.turns().stream()
                .map(turn -> new SavingsGroupStartedIntegrationEvent.Turn(
                        turn.turnNumber(), turn.membershipId(), turn.memberId(), turn.displayName()))
                .toList();
        eventPublisher.publishEvent(new SavingsGroupStartedIntegrationEvent(
                event.groupId(),
                group.getName(),
                event.organizerId(),
                organizerName,
                rules.contribution().amount(),
                rules.contribution().currency(),
                rules.periodicity().name(),
                rules.firstContributionDate(),
                rules.destination().method().name(),
                rules.destination().phoneNumber(),
                turns,
                event.occurredAt()));
    }
}
