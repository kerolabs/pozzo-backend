package pe.kerolabs.pozzo.iam.infrastructure.sms.testnumbers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.kerolabs.pozzo.iam.application.internal.outboundservices.sms.TestPhoneNumbers;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test numbers read from configuration: {@code sms.test-numbers} is a comma-separated list of
 * nine-digit numbers and {@code sms.test-code} the six-digit code they receive.
 */
@Slf4j
@Component
public class ConfiguredTestPhoneNumbers implements TestPhoneNumbers {

    private final Set<String> numbers;
    private final String code;

    public ConfiguredTestPhoneNumbers(@Value("${sms.test-numbers:}") String numbers,
                                      @Value("${sms.test-code:}") String code) {
        this.numbers = Arrays.stream(numbers.split(","))
                .map(String::trim)
                .filter(number -> !number.isEmpty())
                .map(number -> PhoneNumber.ofPeruvianMobile(number).e164())
                .collect(Collectors.toUnmodifiableSet());
        if (!this.numbers.isEmpty() && !code.matches("\\d{6}")) {
            throw new IllegalStateException("sms.test-code must have six digits when sms.test-numbers is set");
        }
        this.code = code;
        if (!this.numbers.isEmpty()) {
            log.warn("{} test phone number(s) enabled: they receive a fixed code and no SMS", this.numbers.size());
        }
    }

    @Override
    public Optional<String> fixedCodeFor(PhoneNumber phoneNumber) {
        return numbers.contains(phoneNumber.e164()) ? Optional.of(code) : Optional.empty();
    }
}
