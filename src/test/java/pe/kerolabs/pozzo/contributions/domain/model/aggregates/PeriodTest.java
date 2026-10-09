package pe.kerolabs.pozzo.contributions.domain.model.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotCompletedEvent;
import pe.kerolabs.pozzo.contributions.domain.model.events.PotDeliveredEvent;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleRules;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.CycleTurn;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.ExpectedStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Money;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.PeriodStatus;
import pe.kerolabs.pozzo.contributions.domain.model.valueobjects.Periodicity;
import pe.kerolabs.pozzo.shared.domain.exceptions.BusinessRuleViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Period: what each member owes, when the pot is complete and its delivery")
class PeriodTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");

    private final UUID anna = UUID.randomUUID();
    private final UUID sofia = UUID.randomUUID();
    private final UUID marta = UUID.randomUUID();
    private final UUID organizerAccount = UUID.randomUUID();
    private Cycle cycle;
    private Period period;

    static CycleRules monthlyRules() {
        return new CycleRules(Money.of(new BigDecimal("200"), Money.PEN), Periodicity.MONTHLY,
                LocalDate.parse("2026-10-20"), "YAPE", "+51987654321", "Anna Weber");
    }

    @BeforeEach
    void openFirstTurn() {
        cycle = Cycle.start(UUID.randomUUID(), "Junta de la familia Weber", organizerAccount, monthlyRules(), List.of(
                new CycleTurn(2, sofia, UUID.randomUUID(), "Sofia Gonzales"),
                new CycleTurn(1, anna, organizerAccount, "Anna Weber"),
                new CycleTurn(3, marta, null, "Marta Quispe")), NOW);
        period = Period.open(cycle, 1, NOW);
    }

    @Test
    void everyMemberOwesTheContributionIncludingTheOneWhoCollects() {
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
        assertThat(period.getPayoutMembershipId()).isEqualTo(anna);
        assertThat(period.getExpected()).hasSize(3).allMatch(expected -> expected.isPending());
        assertThat(period.potAmount().amount()).isEqualByComparingTo("600.00");
        assertThat(period.collected().amount()).isEqualByComparingTo("0.00");
    }

    @Test
    void theCutoffOfTheFirstTurnIsTheFirstContributionDate() {
        assertThat(period.getCutoffDate()).isEqualTo(LocalDate.parse("2026-10-20"));
        assertThat(Period.open(cycle, 2, NOW).getCutoffDate()).isEqualTo(LocalDate.parse("2026-11-20"));
    }

    @Test
    void thePotIsCompleteWhenTheLastMemberSettles() {
        period.settle(anna, ExpectedStatus.PAID, UUID.randomUUID(), NOW);
        period.settle(sofia, ExpectedStatus.PAID, UUID.randomUUID(), NOW);
        assertThat(period.isPotComplete()).isFalse();
        period.clearDomainEvents();

        period.settle(marta, ExpectedStatus.COVERED, UUID.randomUUID(), NOW);

        assertThat(period.getStatus()).isEqualTo(PeriodStatus.POT_COMPLETE);
        assertThat(period.collected().amount()).isEqualByComparingTo("600.00");
        assertThat(period.domainEvents()).singleElement().isInstanceOf(PotCompletedEvent.class);
    }

    @Test
    void aMemberCannotSettleTheSamePeriodTwice() {
        period.settle(sofia, ExpectedStatus.PAID, UUID.randomUUID(), NOW);

        assertThatThrownBy(() -> period.settle(sofia, ExpectedStatus.PAID, UUID.randomUUID(), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("CONTRIBUTION_ALREADY_SETTLED");
    }

    @Test
    void someoneOutsideTheCycleCannotSettle() {
        assertThatThrownBy(() -> period.settle(UUID.randomUUID(), ExpectedStatus.PAID, UUID.randomUUID(), NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("NOT_A_CYCLE_MEMBER");
    }

    @Test
    void thePotCannotBeDeliveredWhileAMemberHasNotPaid() {
        period.settle(anna, ExpectedStatus.PAID, UUID.randomUUID(), NOW);

        assertThatThrownBy(() -> period.deliverPot(organizerAccount, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("POT_NOT_COMPLETE");
    }

    @Test
    void theDeliveryOfACompletePotClosesThePeriodOnce() {
        List.of(anna, sofia, marta).forEach(member -> period.settle(member, ExpectedStatus.PAID, UUID.randomUUID(), NOW));
        period.clearDomainEvents();

        period.deliverPot(organizerAccount, NOW);

        assertThat(period.isDelivered()).isTrue();
        assertThat(period.getDeliveredBy()).isEqualTo(organizerAccount);
        assertThat(period.domainEvents()).singleElement().isInstanceOf(PotDeliveredEvent.class);
        assertThatThrownBy(() -> period.deliverPot(organizerAccount, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .extracting("code").isEqualTo("POT_ALREADY_DELIVERED");
    }
}
