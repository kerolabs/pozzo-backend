package pe.kerolabs.pozzo.savingsgroups.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GroupRules: seats, pot and cutoff dates of a savings group")
class GroupRulesTest {

    private static GroupRules rules(Periodicity periodicity, int seats) {
        return new GroupRules(Money.soles(new BigDecimal("200")), periodicity, seats, LocalDate.parse("2026-10-20"), null);
    }

    @ParameterizedTest(name = "{0} seats are rejected")
    @ValueSource(ints = {0, 1, 51})
    void acceptsBetweenTwoAndFiftySeats(int seats) {
        assertThatThrownBy(() -> rules(Periodicity.MONTHLY, seats)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void thePotIsTheContributionTimesTheSeats() {
        assertThat(rules(Periodicity.MONTHLY, 8).pot().amount()).isEqualByComparingTo("1600");
    }

    @Test
    void eachTurnHasItsCutoffOnePeriodAfterThePrevious() {
        assertThat(rules(Periodicity.MONTHLY, 4).cutoffDateOfTurn(3)).isEqualTo(LocalDate.parse("2026-12-20"));
        assertThat(rules(Periodicity.BIWEEKLY, 4).cutoffDateOfTurn(2)).isEqualTo(LocalDate.parse("2026-11-03"));
        assertThat(rules(Periodicity.WEEKLY, 4).cutoffDateOfTurn(2)).isEqualTo(LocalDate.parse("2026-10-27"));
    }

    @Test
    void theDestinationMustBeAPeruvianMobile() {
        assertThat(Destination.of(PaymentMethod.PLIN, "987 654 321").phoneNumber()).isEqualTo("+51987654321");
        assertThatThrownBy(() -> Destination.of(PaymentMethod.YAPE, "12345")).isInstanceOf(IllegalArgumentException.class);
    }
}
