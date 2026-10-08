package pe.kerolabs.pozzo.notifications.application.internal.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.application.internal.texts.NotificationTexts;
import pe.kerolabs.pozzo.notifications.domain.model.commands.SendAlertCommand;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupDeletedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupFilledIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupMemberJoinedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupRulesUpdatedIntegrationEvent;
import pe.kerolabs.pozzo.savingsgroups.interfaces.events.SavingsGroupStartedIntegrationEvent;

/**
 * Anti-corruption layer: tells the organizer who joined the group, and every member with the application
 * that its rules changed, that it is full, that it started and which turn they got, or that it was deleted.
 */
@Component("notificationsSavingsGroupEventsHandler")
public class SavingsGroupEventsHandler {

    private final NotificationCommandService notificationCommandService;

    public SavingsGroupEventsHandler(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SavingsGroupStartedIntegrationEvent event) {
        event.turns().stream()
                .filter(turn -> turn.accountId() != null)
                .forEach(turn -> notificationCommandService.handle(new SendAlertCommand(turn.accountId(),
                        event.groupId(), null,
                        NotificationTexts.groupStarted(event.groupName(), turn.turnNumber(), event.groupId()),
                        "started:" + event.groupId() + ":" + turn.accountId())));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SavingsGroupMemberJoinedIntegrationEvent event) {
        notificationCommandService.handle(new SendAlertCommand(event.organizerAccountId(), event.groupId(), null,
                NotificationTexts.memberJoined(event.memberName(), event.groupName(), event.membersCount(),
                        event.seats(), event.groupId()),
                "joined:" + event.membershipId() + ":" + event.joinedAt().toEpochMilli()));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SavingsGroupRulesUpdatedIntegrationEvent event) {
        // Saving the rules and the destination of one edit arrive together: one notice per minute is enough.
        var minute = event.updatedAt().toEpochMilli() / 60_000;
        event.memberAccountIds().forEach(accountId -> notificationCommandService.handle(new SendAlertCommand(
                accountId, event.groupId(), null,
                NotificationTexts.rulesUpdated(event.groupName(), event.contributionAmount(), event.periodicity(),
                        event.seats(), event.groupId()),
                "rules:" + event.groupId() + ":" + accountId + ":" + minute)));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SavingsGroupFilledIntegrationEvent event) {
        event.memberAccountIds().forEach(accountId -> notificationCommandService.handle(new SendAlertCommand(
                accountId, event.groupId(), null, NotificationTexts.groupFilled(event.groupName(), event.groupId()),
                "filled:" + event.groupId() + ":" + accountId + ":" + event.filledAt().toEpochMilli())));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SavingsGroupDeletedIntegrationEvent event) {
        event.memberAccountIds().forEach(accountId -> notificationCommandService.handle(new SendAlertCommand(
                accountId, event.groupId(), null, NotificationTexts.groupDeleted(event.groupName()),
                "deleted:" + event.groupId() + ":" + accountId)));
    }
}
