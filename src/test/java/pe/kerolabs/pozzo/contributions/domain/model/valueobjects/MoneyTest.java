package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money: amounts in soles with two decimals")
class MoneyTest {

    private static Money soles(String amount) {
        return Money.of(new BigDecimal(amount), Money.PEN);
    }

    @Test
    void keepsTwoDecimalsAndPrintsInSoles() {
        assertThat(soles("200").toString()).isEqualTo("S/ 200.00");
        assertThat(soles("200").isSameAmountAs(soles("200.00"))).isTrue();
    }

    @Test
    void addsAndSubtractsWithoutGoingBelowZero() {
        assertThat(soles("150").plus(soles("50")).amount()).isEqualByComparingTo("200");
        assertThat(soles("50").minus(soles("80")).amount()).isEqualByComparingTo("0");
        assertThat(soles("200").times(4).amount()).isEqualByComparingTo("800");
    }

    @Test
    void rejectsNegativeAmountsAndMoreThanTwoDecimals() {
        assertThatThrownBy(() -> soles("-1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> soles("10.001")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void doesNotMixCurrencies() {
        var dollars = Money.of(new BigDecimal("10"), "USD");

        assertThatThrownBy(() -> soles("10").plus(dollars)).isInstanceOf(IllegalArgumentException.class);
        assertThat(soles("10").isSameAmountAs(dollars)).isFalse();
    }
}
