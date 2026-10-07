package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import pe.kerolabs.pozzo.contributions.domain.model.entities.ExpectedContribution;
import pe.kerolabs.pozzo.contributions.domain.model.events.PeriodOpenedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotCompletedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotDeliveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PeriodStatus;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Period aggregate root: one turn of the cycle.
 *
 * <p>When it opens it knows what each member owes. It records who has settled, decides when the pot
 * is complete, and is closed when the organizer confirms the pot was handed to the member who
 * collects. Every member contributes, including the one who collects in that turn.</p>
 */
@Getter
public class Period extends AbstractDomainAggregateRoot<Period> {

    private UUID id;
    private UUID cycleId;
    private int turnNumber;
    private LocalDate opensAt;
    private LocalDate cutoffDate;
    private UUID payoutMembershipId;
    private List<ExpectedContribution> expected = new ArrayList<>();
    private PeriodStatus status;
    private @Nullable Instant deliveredAt;
    private @Nullable UUID deliveredBy;

    public Period() {
    }

    /**
     * Opens a turn of the cycle: every member owes the contribution of the rules.
     */
    public static Period open(Cycle cycle, int turnNumber, Instant now) {
        var rules = cycle.getRules();
        var period = new Period();
        period.id = UUID.randomUUID();
        period.cycleId = cycle.getId();
        period.turnNumber = turnNumber;
        period.opensAt = rules.opensAtOfTurn(turnNumber);
        period.cutoffDate = rules.cutoffDateOfTurn(turnNumber);
        period.payoutMembershipId = cycle.turn(turnNumber).membershipId();
        cycle.getTurns().forEach(turn ->
                period.expected.add(ExpectedContribution.pendingFor(turn.membershipId(), rules.contribution())));
        period.status = PeriodStatus.OPEN;
        period.registerDomainEvent(new PeriodOpenedEvent(period.id, cycle.getId(), cycle.getGroupId(), turnNumber,
                period.payoutMembershipId, period.cutoffDate, now));
        return period;
    }

    public Optional<ExpectedContribution> expectedFor(UUID membershipId) {
        return expected.stream().filter(item -> item.getMembershipId().equals(membershipId)).findFirst();
    }

    public boolean isOpen() {
        return status == PeriodStatus.OPEN;
    }

    public boolean isPotComplete() {
        return expected.stream().noneMatch(ExpectedContribution::isPending);
    }

    public boolean isDelivered() {
        return status == PeriodStatus.DELIVERED;
    }

    public List<ExpectedContribution> pendingMembers() {
        return expected.stream().filter(ExpectedContribution::isPending).toList();
    }

    public int settledCount() {
        return (int) expected.stream().filter(item -> !item.isPending()).count();
    }

    /**
     * The amount gathered so far: what the members who already settled owed.
     */
    public Money collected() {
        return expected.stream().filter(item -> !item.isPending())
                .map(ExpectedContribution::getAmount)
                .reduce(Money::plus)
                .orElseGet(() -> Money.zero(potAmount().currency()));
    }

    /**
     * The amount the member who collects receives when every member has settled.
     */
    public Money potAmount() {
        return expected.stream().map(ExpectedContribution::getAmount).reduce(Money::plus).orElseThrow();
    }

    /**
     * Records that a member settled what was owed. When it is the last one, the pot is complete.
     */
    public void settle(UUID membershipId, ExpectedStatus how, UUID contributionId, Instant now) {
        var item = expectedFor(membershipId).orElseThrow(() -> new BusinessRuleViolationException(
                "NOT_A_CYCLE_MEMBER", "The member does not take part in this cycle"));
        if (!item.isPending()) {
            throw new BusinessRuleViolationException("CONTRIBUTION_ALREADY_SETTLED",
                    "The member already settled this period");
        }
        item.settle(how, contributionId);
        if (status == PeriodStatus.OPEN && isPotComplete()) {
            status = PeriodStatus.POT_COMPLETE;
            registerDomainEvent(new PotCompletedEvent(id, cycleId, turnNumber, now));
        }
    }

    /**
     * Confirms that the organizer handed the pot to the member who collects in this turn.
     * Pozzo never moves the money: it only records the confirmation.
     */
    public void deliverPot(UUID organizerAccountId, Instant now) {
        if (status == PeriodStatus.DELIVERED) {
            throw new BusinessRuleViolationException("POT_ALREADY_DELIVERED", "The pot of this period was delivered");
        }
        if (status != PeriodStatus.POT_COMPLETE) {
            throw new BusinessRuleViolationException("POT_NOT_COMPLETE",
                    "Every member must pay or be covered before delivering the pot");
        }
        this.status = PeriodStatus.DELIVERED;
        this.deliveredAt = now;
        this.deliveredBy = organizerAccountId;
        registerDomainEvent(new PotDeliveredEvent(id, cycleId, turnNumber, payoutMembershipId, now));
    }

    public List<ExpectedContribution> getExpected() {
        return Collections.unmodifiableList(expected);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID cycleId, int turnNumber, LocalDate opensAt, LocalDate cutoffDate,
                             UUID payoutMembershipId, List<ExpectedContribution> expected, PeriodStatus status,
                             @Nullable Instant deliveredAt, @Nullable UUID deliveredBy) {
        this.id = id;
        this.cycleId = cycleId;
        this.turnNumber = turnNumber;
        this.opensAt = opensAt;
        this.cutoffDate = cutoffDate;
        this.payoutMembershipId = payoutMembershipId;
        this.expected = new ArrayList<>(expected);
        this.status = status;
        this.deliveredAt = deliveredAt;
        this.deliveredBy = deliveredBy;
    }
}
