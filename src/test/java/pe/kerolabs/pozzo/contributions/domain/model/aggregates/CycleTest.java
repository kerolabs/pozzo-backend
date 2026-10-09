package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.domain.model.events.CycleClosedEvent;
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
}
