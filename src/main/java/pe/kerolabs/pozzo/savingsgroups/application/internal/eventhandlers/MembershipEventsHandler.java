package pe.kerolabs.pozzo.savingsgroups.application.internal.eventhandlers;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.savingsgroups.domain.model.entities.Membership;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.GroupDeletedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.model.events.MemberJoinedEvent;
import pe.kerolabs.pozzo.savingsgroups.domain.repositories.SavingsGroupRepository;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupDeletedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupMemberJoinedIntegrationEvent;

/**
 * Translates the joins and the deletion of a group into integration events for the other contexts.
 */
@Component
public class MembershipEventsHandler {

    private final SavingsGroupRepository savingsGroupRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MembershipEventsHandler(SavingsGroupRepository savingsGroupRepository,
                                   ApplicationEventPublisher eventPublisher) {
        this.savingsGroupRepository = savingsGroupRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(MemberJoinedEvent event) {
        savingsGroupRepository.findById(event.groupId()).ifPresent(group -> {
            var memberName = group.findMembership(event.membershipId())
                    .map(Membership::getDisplayName)
                    .orElse("");
            eventPublisher.publishEvent(new SavingsGroupMemberJoinedIntegrationEvent(
                    group.getId(),
                    group.getName(),
                    group.getOrganizerId(),
                    event.membershipId(),
                    memberName,
                    group.activeMembersCount(),
                    group.getRules().seats(),
                    event.occurredAt()));
        });
    }

    @EventListener
    public void on(GroupDeletedEvent event) {
        eventPublisher.publishEvent(new SavingsGroupDeletedIntegrationEvent(
                event.groupId(), event.groupName(), event.memberAccountIds(), event.occurredAt()));
    }
}
