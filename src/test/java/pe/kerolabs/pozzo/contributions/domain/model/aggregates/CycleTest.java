package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleClosedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleMemberLinkedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.TurnsSwappedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cycle: the turn in progress and the end of the cycle")
class CycleTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");

    private final UUID organizer = UUID.randomUUID();

    private Cycle twoTurnCycle() {
        return Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizer, PeriodTest.monthlyRules(), List.of(
                new CycleTurn(1, UUID.randomUUID(), organizer, "Anna Weber"),
                new CycleTurn(2, UUID.randomUUID(), UUID.randomUUID(), "Sofia Gonzales")), NOW);
    }

    @Test
    void startsActiveOnTheFirstTurn() {
        var cycle = twoTurnCycle();

        assertThat(cycle.getStatus()).isEqualTo(CycleStatus.ACTIVE);
        assertThat(cycle.getCurrentTurn()).isEqualTo(1);
        assertThat(cycle.totalTurns()).isEqualTo(2);
        assertThat(cycle.isOrganizer(organizer)).isTrue();
        assertThat(cycle.isParticipant(organizer)).isTrue();
    }

    @Test
    void needsAtLeastTwoMembers() {
        assertThatThrownBy(() -> Cycle.start(UUID.randomUUID(), "Junta", organizer, PeriodTest.monthlyRules(),
                List.of(new CycleTurn(1, UUID.randomUUID(), organizer, "Anna Weber")), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotCloseBeforeTheLastTurn() {
        var cycle = twoTurnCycle();

        assertThatThrownBy(() -> cycle.close(NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("CYCLE_HAS_REMAINING_TURNS");
    }

    @Test
    void closesAfterEveryMemberCollected() {
        var cycle = twoTurnCycle();
        cycle.clearDomainEvents();

        assertThat(cycle.advanceToNextTurn()).isEqualTo(2);
        assertThat(cycle.hasRemainingTurns()).isFalse();
        cycle.close(NOW);

        assertThat(cycle.getStatus()).isEqualTo(CycleStatus.CLOSED);
        assertThat(cycle.domainEvents()).singleElement().isInstanceOf(CycleClosedEvent.class);
        assertThatThrownBy(cycle::advanceToNextTurn)
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("NO_REMAINING_TURNS");
    }

    @Test
    void aMemberWithoutTheApplicationGetsTheTurnForTheAccountTheySignedUpWith() {
        var marta = UUID.randomUUID();
        var cycle = Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizer, PeriodTest.monthlyRules(),
                List.of(new CycleTurn(1, UUID.randomUUID(), organizer, "Anna Weber"),
                        new CycleTurn(2, UUID.randomUUID(), null, "Marta Quispe")), NOW);
        var membership = cycle.turn(2).membershipId();
        cycle.clearDomainEvents();

        assertThat(cycle.linkAccount(membership, marta, NOW)).isTrue();

        assertThat(cycle.participantOf(marta)).hasValueSatisfying(turn -> assertThat(turn.turnNumber()).isEqualTo(2));
        assertThat(cycle.domainEvents()).singleElement().isInstanceOf(CycleMemberLinkedEvent.class);
        assertThat(cycle.linkAccount(membership, marta, NOW)).isFalse();
        assertThat(cycle.linkAccount(UUID.randomUUID(), marta, NOW)).isFalse();
    }

    @Test
    void theOrganizerSwapsTwoTurnsWhosePeriodsHaveNotStarted() {
        var cycle = Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizer, PeriodTest.monthlyRules(),
                List.of(new CycleTurn(1, UUID.randomUUID(), organizer, "Anna Weber"),
                        new CycleTurn(2, UUID.randomUUID(), UUID.randomUUID(), "Sofia Gonzales"),
                        new CycleTurn(3, UUID.randomUUID(), UUID.randomUUID(), "Jorge Ramos")), NOW);
        var sofia = cycle.turn(2).membershipId();
        var jorge = cycle.turn(3).membershipId();
        cycle.clearDomainEvents();

        cycle.swapTurns(sofia, jorge, NOW);

        assertThat(cycle.turn(2).membershipId()).isEqualTo(jorge);
        assertThat(cycle.turn(3).membershipId()).isEqualTo(sofia);
        assertThat(cycle.domainEvents()).singleElement().isInstanceOf(TurnsSwappedEvent.class);
    }

    @Test
    void aTurnWhosePeriodStartedOrTheSameMemberCannotBeSwapped() {
        var cycle = twoTurnCycle();
        var first = cycle.turn(1).membershipId();
        var second = cycle.turn(2).membershipId();

        assertThatThrownBy(() -> cycle.swapTurns(first, second, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("TURN_ALREADY_STARTED");
        assertThatThrownBy(() -> cycle.swapTurns(second, second, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("SAME_MEMBER");
    }

    @Test
    void theReplacementTakesATurnThatWasNotCollected() {
        var cycle = twoTurnCycle();
        var first = cycle.turn(1).membershipId();
        var second = cycle.turn(2).membershipId();
        var rosa = UUID.randomUUID();

        assertThatThrownBy(() -> cycle.replaceMember(first, UUID.randomUUID(), null, "Rosa Medina", true))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("TURN_ALREADY_COLLECTED");

        var turn = cycle.replaceMember(second, rosa, null, "Rosa Medina", false);

        assertThat(turn.turnNumber()).isEqualTo(2);
        assertThat(cycle.turn(2).membershipId()).isEqualTo(rosa);
    }

    @Test
    void theWinnerOfAnAuctionTakesTheTurnInProgressFromItsProvisionalHolder() {
        var cycle = twoTurnCycle();
        var first = cycle.turn(1).membershipId();
        var second = cycle.turn(2).membershipId();

        cycle.giveCurrentTurnTo(second);

        assertThat(cycle.turn(1).membershipId()).isEqualTo(second);
        assertThat(cycle.turn(2).membershipId()).isEqualTo(first);
    }
}
