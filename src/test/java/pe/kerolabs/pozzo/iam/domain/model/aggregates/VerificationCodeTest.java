package pe.kerolabs.pozzo.iam.domain.model.aggregates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.kerolabs.pozzo.iam.domain.model.events.CodeVerifiedEvent;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.PhoneNumber;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationOutcome;
import pe.kerolabs.pozzo.iam.domain.model.valueobjects.VerificationStatus;
import pe.kerolabs.pozzo.iam.domain.services.CodeMatcher;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VerificationCode: the SMS code that proves the phone belongs to the member")
class VerificationCodeTest {

    private static final Instant ISSUED = Instant.parse("2026-10-08T15:00:00Z");
    private static final CodeMatcher RIGHT = input -> true;
    private static final CodeMatcher WRONG = input -> false;

    private VerificationCode code;

    @BeforeEach
    void issue() {
        code = VerificationCode.issue(PhoneNumber.ofPeruvianMobile("987654321"), "hash", ISSUED);
        code.clearDomainEvents();
    }

    @Test
    void lastsTenMinutesAndCanBeReplacedAfterThirtySeconds() {
        assertThat(code.getExpiresAt()).isEqualTo(ISSUED.plus(Duration.ofMinutes(10)));
        assertThat(code.canBeReplacedAt(ISSUED.plusSeconds(29))).isFalse();
        assertThat(code.canBeReplacedAt(ISSUED.plusSeconds(30))).isTrue();
    }

    @Test
    void theRightCodeVerifiesThePhone() {
        assertThat(code.verify("482913", ISSUED.plusSeconds(60), RIGHT)).isEqualTo(VerificationOutcome.VERIFIED);
        assertThat(code.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(code.domainEvents()).singleElement().isInstanceOf(CodeVerifiedEvent.class);
    }

    @Test
    void aWrongCodeLeavesFewerAttempts() {
        assertThat(code.verify("000000", ISSUED.plusSeconds(60), WRONG)).isEqualTo(VerificationOutcome.INVALID);
        assertThat(code.remainingAttempts()).isEqualTo(2);
    }

    @Test
    void theThirdWrongCodeBlocksIt() {
        code.verify("000000", ISSUED.plusSeconds(60), WRONG);
        code.verify("000000", ISSUED.plusSeconds(61), WRONG);

        assertThat(code.verify("000000", ISSUED.plusSeconds(62), WRONG)).isEqualTo(VerificationOutcome.BLOCKED);
        assertThat(code.verify("482913", ISSUED.plusSeconds(63), RIGHT)).isEqualTo(VerificationOutcome.BLOCKED);
    }

    @Test
    void anExpiredCodeIsNotAccepted() {
        assertThat(code.verify("482913", ISSUED.plus(Duration.ofMinutes(10)), RIGHT))
                .isEqualTo(VerificationOutcome.EXPIRED);
        assertThat(code.getStatus()).isEqualTo(VerificationStatus.EXPIRED);
    }

    @Test
    void aReplacedCodeCanNoLongerBeUsed() {
        code.invalidate();

        assertThat(code.verify("482913", ISSUED.plusSeconds(60), RIGHT)).isEqualTo(VerificationOutcome.EXPIRED);
    }
}
