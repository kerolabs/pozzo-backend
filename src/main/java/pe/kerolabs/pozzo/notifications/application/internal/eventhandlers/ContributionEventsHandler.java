package pe.kerolabs.pozzo.notifications.application.internal.eventhandlers;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionReviewIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionSettledIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleClosedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleMember;
import pe.kerolabs.pozzo.contributions.interfaces.events.PeriodOpenedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.PotIntegrationEvent;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.application.internal.texts.NotificationTexts;
import pe.kerolabs.pozzo.notifications.domain.model.commands.CancelRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.ScheduleRemindersCommand;
import pe.kerolabs.pozzo.notifications.domain.model.commands.SendAlertCommand;

import java.util.Objects;

/**
 * Anti-corruption layer: turns the events of Contributions into reminders and alerts.
 *
 * <p>It runs once the contribution is committed and in its own transaction, so a notification never
 * undoes a payment and nothing is announced that did not happen. Members without the application
 * have no account and are skipped.</p>
 */
@Component("notificationsContributionEventsHandler")
public class ContributionEventsHandler {

    private final NotificationCommandService notificationCommandService;

    public ContributionEventsHandler(NotificationCommandService notificationCommandService) {
        this.notificationCommandService = notificationCommandService;
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PeriodOpenedIntegrationEvent event) {
        var accountIds = event.members().stream()
                .map(CycleMember::accountId)
                .filter(Objects::nonNull)
                .toList();
        notificationCommandService.handle(new ScheduleRemindersCommand(event.groupId(), event.groupName(),
                event.periodId(), event.cutoffDate(), event.contributionAmount(), accountIds));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ContributionSettledIntegrationEvent event) {
        var accountId = event.member().accountId();
        if (accountId == null) {
            return;
        }
        notificationCommandService.handle(new CancelRemindersCommand(event.periodId(), accountId));
        var content = ContributionSettledIntegrationEvent.COVERED.equals(event.outcome())
                ? NotificationTexts.contributionCovered(event.groupName(), event.amount(), event.groupId())
                : NotificationTexts.contributionSettled(event.groupName(), event.amount(),
                ContributionSettledIntegrationEvent.LATE.equals(event.outcome()), event.groupId());
        notificationCommandService.handle(new SendAlertCommand(accountId, event.groupId(), event.periodId(), content,
                "settled:" + event.contributionId()));
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ContributionReviewIntegrationEvent event) {
        if (ContributionReviewIntegrationEvent.REQUIRED.equals(event.stage())) {
            notificationCommandService.handle(new SendAlertCommand(event.organizerAccountId(), event.groupId(),
                    event.periodId(),
                    NotificationTexts.reviewRequired(event.member().displayName(), event.groupName(), event.groupId()),
                    "review:" + event.contributionId()));
            return;
        }
        var accountId = event.member().accountId();
        if (accountId != null) {
            notificationCommandService.handle(new SendAlertCommand(accountId, event.groupId(), event.periodId(),
                    NotificationTexts.contributionRejected(event.groupName(), event.groupId()),
                    "rejected:" + event.contributionId()));
        }
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PotIntegrationEvent event) {
        if (PotIntegrationEvent.COMPLETED.equals(event.stage())) {
            notificationCommandService.handle(new SendAlertCommand(event.organizerAccountId(), event.groupId(),
                    event.periodId(),
                    NotificationTexts.potCompleted(event.groupName(), event.turnNumber(), event.potAmount(),
                            event.payout().displayName(), event.groupId()),
                    "pot-completed:" + event.periodId()));
            return;
        }
        var accountId = event.payout().accountId();
        if (accountId != null) {
            notificationCommandService.handle(new SendAlertCommand(accountId, event.groupId(), event.periodId(),
                    NotificationTexts.potDelivered(event.groupName(), event.potAmount(), event.groupId()),
                    "pot-delivered:" + event.periodId()));
        }
    }

    @TransactionalEventListener(fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(CycleClosedIntegrationEvent event) {
        event.members().stream()
                .filter(member -> member.accountId() != null)
                .forEach(member -> notificationCommandService.handle(new SendAlertCommand(member.accountId(),
                        event.groupId(), null, NotificationTexts.cycleClosed(event.groupName()),
                        "cycle-closed:" + event.cycleId() + ":" + member.accountId())));
    }
}
