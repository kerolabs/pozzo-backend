package pe.kerolabs.pozzo.contributions.application.internal.eventhandlers;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionCoveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionRejectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.ContributionValidatedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleClosedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.InconsistencyDetectedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PeriodOpenedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotCompletedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotDeliveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.repositories.CycleRepository;
import pe.kerolabs.pozzo.contributions.domain.repositories.PeriodRepository;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionReviewIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.ContributionSettledIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleClosedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.CycleMember;
import pe.kerolabs.pozzo.contributions.interfaces.events.PeriodOpenedIntegrationEvent;
import pe.kerolabs.pozzo.contributions.interfaces.events.PotIntegrationEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Translates the domain events of Contributions into integration events for the other contexts.
 * It adds what they need and the domain events do not carry (the name of the group, the account and
 * name of each member) so the consumers never read the Contributions model. It runs in the same
 * transaction as the change that raised the event.
 */
@Component
public class IntegrationEventsPublisher {

    private final CycleRepository cycleRepository;
    private final PeriodRepository periodRepository;
    private final ApplicationEventPublisher eventPublisher;

    public IntegrationEventsPublisher(CycleRepository cycleRepository, PeriodRepository periodRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.cycleRepository = cycleRepository;
        this.periodRepository = periodRepository;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void on(PeriodOpenedEvent event) {
        var cycle = cycle(event.cycleId());
        eventPublisher.publishEvent(new PeriodOpenedIntegrationEvent(event.periodId(), cycle.getId(),
                cycle.getGroupId(), cycle.getGroupName(), event.turnNumber(), cycle.totalTurns(), event.cutoffDate(),
                cycle.getRules().contribution().amount(), member(cycle, event.payoutMembershipId()),
                cycle.getOrganizerAccountId(), members(cycle), event.occurredAt()));
    }

    @EventListener
    public void on(ContributionValidatedEvent event) {
        var cycle = cycle(event.cycleId());
        var period = period(event.periodId());
        var outcome = event.onTime() ? ContributionSettledIntegrationEvent.ON_TIME : ContributionSettledIntegrationEvent.LATE;
        eventPublisher.publishEvent(new ContributionSettledIntegrationEvent(event.contributionId(), cycle.getId(),
                cycle.getGroupId(), cycle.getGroupName(), event.periodId(), period.getTurnNumber(),
                member(cycle, event.membershipId()), outcome, cycle.getRules().contribution().amount(),
                event.occurredAt()));
    }

    @EventListener
    public void on(ContributionCoveredEvent event) {
        var cycle = cycle(event.cycleId());
        var period = period(event.periodId());
        eventPublisher.publishEvent(new ContributionSettledIntegrationEvent(event.contributionId(), cycle.getId(),
                cycle.getGroupId(), cycle.getGroupName(), event.periodId(), period.getTurnNumber(),
                member(cycle, event.membershipId()), ContributionSettledIntegrationEvent.COVERED,
                cycle.getRules().contribution().amount(), event.occurredAt()));
    }

    @EventListener
    public void on(InconsistencyDetectedEvent event) {
        var cycle = cycle(event.cycleId());
        eventPublisher.publishEvent(new ContributionReviewIntegrationEvent(event.contributionId(), cycle.getId(),
                cycle.getGroupId(), cycle.getGroupName(), event.periodId(), member(cycle, event.membershipId()),
                cycle.getOrganizerAccountId(), ContributionReviewIntegrationEvent.REQUIRED, event.occurredAt()));
    }

    @EventListener
    public void on(ContributionRejectedEvent event) {
        var cycle = cycle(event.cycleId());
        eventPublisher.publishEvent(new ContributionReviewIntegrationEvent(event.contributionId(), cycle.getId(),
                cycle.getGroupId(), cycle.getGroupName(), event.periodId(), member(cycle, event.membershipId()),
                cycle.getOrganizerAccountId(), ContributionReviewIntegrationEvent.REJECTED, event.occurredAt()));
    }

    @EventListener
    public void on(PotCompletedEvent event) {
        publishPot(event.periodId(), event.cycleId(), PotIntegrationEvent.COMPLETED, event.occurredAt());
    }

    @EventListener
    public void on(PotDeliveredEvent event) {
        publishPot(event.periodId(), event.cycleId(), PotIntegrationEvent.DELIVERED, event.occurredAt());
    }

    @EventListener
    public void on(CycleClosedEvent event) {
        var cycle = cycle(event.cycleId());
        eventPublisher.publishEvent(new CycleClosedIntegrationEvent(cycle.getId(), cycle.getGroupId(),
                cycle.getGroupName(), members(cycle), event.occurredAt()));
    }

    private void publishPot(UUID periodId, UUID cycleId, String stage, Instant occurredAt) {
        var cycle = cycle(cycleId);
        var period = period(periodId);
        eventPublisher.publishEvent(new PotIntegrationEvent(periodId, cycle.getId(), cycle.getGroupId(),
                cycle.getGroupName(), period.getTurnNumber(), member(cycle, period.getPayoutMembershipId()),
                cycle.getOrganizerAccountId(), period.potAmount().amount(), stage, occurredAt));
    }

    private Cycle cycle(UUID cycleId) {
        return cycleRepository.findById(cycleId).orElseThrow();
    }

    private Period period(UUID periodId) {
        return periodRepository.findById(periodId).orElseThrow();
    }

    private static CycleMember member(Cycle cycle, UUID membershipId) {
        return cycle.findParticipant(membershipId).map(IntegrationEventsPublisher::toMember).orElseThrow();
    }

    private static List<CycleMember> members(Cycle cycle) {
        return cycle.getTurns().stream().map(IntegrationEventsPublisher::toMember).toList();
    }

    private static CycleMember toMember(CycleTurn turn) {
        return new CycleMember(turn.membershipId(), turn.accountId(), turn.displayName());
    }
}
