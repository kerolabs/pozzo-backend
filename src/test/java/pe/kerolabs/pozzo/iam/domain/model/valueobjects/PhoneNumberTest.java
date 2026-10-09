package pe.kerolabs.pozzo.iam.domain.model.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PhoneNumber: Peruvian mobile numbers, the identity of a member")
class PhoneNumberTest {

    @Test
    void acceptsANineDigitMobileWithOrWithoutSpaces() {
        assertThat(PhoneNumber.ofPeruvianMobile("987 654 321").e164()).isEqualTo("+51987654321");
        assertThat(PhoneNumber.fromE164("+51987654321").number()).isEqualTo("987654321");
    }

    @ParameterizedTest(name = "\"{0}\" is rejected")
    @ValueSource(strings = {"887654321", "98765432", "9876543210", "abcdefghi"})
    void rejectsNumbersThatAreNotPeruvianMobiles(String number) {
        assertThatThrownBy(() -> PhoneNumber.ofPeruvianMobile(number)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNumbersFromOtherCountries() {
        assertThatThrownBy(() -> PhoneNumber.fromE164("+34612345678")).isInstanceOf(IllegalArgumentException.class);
    }
}
