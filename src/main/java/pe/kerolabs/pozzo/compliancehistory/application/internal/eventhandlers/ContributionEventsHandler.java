package pe.kerolabs.pozzo.compliancehistory.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.compliancehistory.application.commandservices.ComplianceCommandService;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RecordComplianceEntryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionReviewIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionSettledIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleClosedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleMemberDroppedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleMemberLinkedIntegrationEvent;

/**
 * Anti-corruption layer: translates the events of Contributions, expressed in contributions and
 * periods, into compliance facts. Members without the application have no history and are skipped.
 */
@Component
public class ContributionEventsHandler {

    private final ComplianceCommandService complianceCommandService;

    public ContributionEventsHandler(ComplianceCommandService complianceCommandService) {
        this.complianceCommandService = complianceCommandService;
    }

    @EventListener
    public void on(ContributionSettledIntegrationEvent event) {
        var accountId = event.member().accountId();
        if (accountId == null) {
            return;
        }
        complianceCommandService.handle(new RecordComplianceEntryCommand(accountId, event.cycleId(), event.groupId(),
                event.groupName(), event.periodId(), kindOf(event.outcome()), event.occurredAt(),
                "settled:" + event.contributionId()));
    }

    @EventListener
    public void on(ContributionReviewIntegrationEvent event) {
        var accountId = event.member().accountId();
        if (accountId == null || !ContributionReviewIntegrationEvent.REJECTED.equals(event.stage())) {
            return;
        }
        complianceCommandService.handle(new RecordComplianceEntryCommand(accountId, event.cycleId(), event.groupId(),
                event.groupName(), event.periodId(), EntryKind.REJECTED, event.occurredAt(),
                "rejected:" + event.contributionId()));
    }

    /**
     * A member who took part without the application linked an account: their contributions so far and, if the
     * cycle is over, the completed cycle become part of the history of that account. The sources are the same
     * as for a member with the application, so nothing is recorded twice.
     */
    @EventListener
    public void on(CycleMemberLinkedIntegrationEvent event) {
        event.settled().forEach(contribution -> complianceCommandService.handle(new RecordComplianceEntryCommand(
                event.accountId(), event.cycleId(), event.groupId(), event.groupName(), contribution.periodId(),
                kindOf(contribution.outcome()), contribution.settledAt(), "settled:" + contribution.contributionId())));
        if (event.cycleClosed()) {
            complianceCommandService.handle(new RecordComplianceEntryCommand(event.accountId(), event.cycleId(),
                    event.groupId(), event.groupName(), null, EntryKind.CYCLE_COMPLETED, event.occurredAt(),
                    "cycle-closed:" + event.cycleId() + ":" + event.accountId()));
        }
    }

    @EventListener
    public void on(CycleMemberDroppedIntegrationEvent event) {
        var accountId = event.member().accountId();
        if (accountId == null) {
            return;
        }
        complianceCommandService.handle(new RecordComplianceEntryCommand(accountId, event.cycleId(), event.groupId(),
                event.groupName(), null, EntryKind.DROPOUT, event.occurredAt(),
                "dropout:" + event.cycleId() + ":" + event.member().membershipId()));
    }

    @EventListener
    public void on(CycleClosedIntegrationEvent event) {
        event.members().stream()
                .filter(member -> member.accountId() != null)
                .forEach(member -> complianceCommandService.handle(new RecordComplianceEntryCommand(
                        member.accountId(), event.cycleId(), event.groupId(), event.groupName(), null,
                        EntryKind.CYCLE_COMPLETED, event.occurredAt(),
                        "cycle-closed:" + event.cycleId() + ":" + member.accountId())));
    }

    private static EntryKind kindOf(String outcome) {
        return switch (outcome) {
            case ContributionSettledIntegrationEvent.ON_TIME -> EntryKind.ON_TIME;
            case ContributionSettledIntegrationEvent.LATE -> EntryKind.LATE;
            default -> EntryKind.COVERED;
        };
    }
}
