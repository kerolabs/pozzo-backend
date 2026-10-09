package pe.kerolabs.pozzo.contributions.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentReceipt: the recipient printed by Yape, Plin or the bank")
class PaymentReceiptTest {

    private static PaymentReceipt toPayee(String payeeOnReceipt) {
        return new PaymentReceipt("04581273", null, payeeOnReceipt, Money.of(new BigDecimal("200"), Money.PEN),
                LocalDate.parse("2026-10-08"), ReceiptSource.YAPE);
    }

    @ParameterizedTest(name = "\"{0}\" matches \"{1}\": {2}")
    @CsvSource({
            "Anna Weber, Anna Weber, true",
            "ANNA WEBER, Anna Weber, true",
            "Sofía Gonzáles, Sofia Gonzales, true",
            "Anna W*, Anna Weber, true",
            "Anna Weber Ruiz, Anna Weber, true",
            "Carla Vega, Anna Weber, false",
            "Anna, Anna Weber, false"
    })
    void matchesThePayeeIgnoringCaseAccentsAndTheMaskedSurname(String payeeOnReceipt, String expected, boolean matches) {
        assertThat(toPayee(payeeOnReceipt).matchesPayee(expected)).isEqualTo(matches);
    }
}
