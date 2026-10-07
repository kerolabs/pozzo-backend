package pe.kerolabs.pozzo.compliancehistory.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.compliancehistory.application.commandservices.ComplianceCommandService;
import pe.kerolabs.pozzo.compliancehistory.domain.model.commands.RecordComplianceEntryCommand;
import pe.kerolabs.pozzo.compliancehistory.domain.model.valueobjects.EntryKind;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionReviewIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionSettledIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleClosedIntegrationEvent;

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
        var kind = switch (event.outcome()) {
            case ContributionSettledIntegrationEvent.ON_TIME -> EntryKind.ON_TIME;
            case ContributionSettledIntegrationEvent.LATE -> EntryKind.LATE;
            default -> EntryKind.COVERED;
        };
        complianceCommandService.handle(new RecordComplianceEntryCommand(accountId, event.cycleId(), event.groupId(),
                event.groupName(), event.periodId(), kind, event.occurredAt(), "settled:" + event.contributionId()));
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

    @EventListener
    public void on(CycleClosedIntegrationEvent event) {
        event.members().stream()
                .filter(member -> member.accountId() != null)
                .forEach(member -> complianceCommandService.handle(new RecordComplianceEntryCommand(
                        member.accountId(), event.cycleId(), event.groupId(), event.groupName(), null,
                        EntryKind.CYCLE_COMPLETED, event.occurredAt(),
                        "cycle-closed:" + event.cycleId() + ":" + member.accountId())));
    }
}
