package pe.kerolabs.pozzo.contributions.interfaces.rest.transform;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.application.queryservices.PeriodView;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Cycle;
import pe.kerolabs.pozzo.contributions.domain.model.aggregates.Period;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.MyContributionsResource;
import pe.kerolabs.pozzo.contributions.interfaces.rest.resources.PeriodStatusResource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Period status: the members a period shows")
class PeriodStatusResourceFromViewAssemblerTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");
    private static final LocalDate TODAY = LocalDate.parse("2026-10-08");

    private final UUID organizerAccount = UUID.randomUUID();
    private final UUID anna = UUID.randomUUID();
    private final UUID sofia = UUID.randomUUID();
    private final UUID rosa = UUID.randomUUID();
    private final UUID rosaAccount = UUID.randomUUID();

    private Cycle twoTurnCycle() {
        var rules = new CycleRules(Money.of(new BigDecimal("200"), Money.PEN), Periodicity.MONTHLY,
                LocalDate.parse("2026-10-20"), "YAPE", "+51987654321", "Anna Weber");
        return Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizerAccount, rules, List.of(
                new CycleTurn(1, anna, organizerAccount, "Anna Weber"),
                new CycleTurn(2, sofia, UUID.randomUUID(), "Sofia Gonzales")), NOW);
    }

    @Test
    void aPeriodOpenedBeforeAReplacementShowsOnlyTheMembersWhoOwedInIt() {
        var cycle = twoTurnCycle();
        var firstPeriod = Period.open(cycle, 1, NOW);
        cycle.replaceMember(sofia, rosa, rosaAccount, "Rosa Medina", false);

        var resource = PeriodStatusResourceFromViewAssembler.toResourceFromView(
                new PeriodView(cycle, firstPeriod, List.of()), organizerAccount, TODAY);

        assertThat(resource.members()).extracting(PeriodStatusResource.MemberStatus::membershipId)
                .containsExactly(anna);
    }

    @Test
    void theReplacementCanReadAPeriodThatDidNotExpectThem() {
        var cycle = twoTurnCycle();
        var firstPeriod = Period.open(cycle, 1, NOW);
        cycle.replaceMember(sofia, rosa, rosaAccount, "Rosa Medina", false);

        var resource = PeriodStatusResourceFromViewAssembler.toResourceFromView(
                new PeriodView(cycle, firstPeriod, List.of()), rosaAccount, TODAY);

        assertThat(resource.members()).noneMatch(PeriodStatusResource.MemberStatus::me);
    }

    @Test
    void theContributionsOfAReplacementStartInThePeriodTheyJoined() {
        var cycle = twoTurnCycle();
        var firstPeriod = Period.open(cycle, 1, NOW);
        cycle.replaceMember(sofia, rosa, rosaAccount, "Rosa Medina", false);
        cycle.advanceToNextTurn();
        var secondPeriod = Period.open(cycle, 2, NOW);

        var resource = MyContributionsResourceFromViewAssembler.toResourceFromViews(cycle, List.of(
                new PeriodView(cycle, firstPeriod, List.of()),
                new PeriodView(cycle, secondPeriod, List.of())), rosaAccount, TODAY);

        assertThat(resource.periods()).extracting(MyContributionsResource.PeriodItem::turnNumber).containsExactly(2);
    }
}
