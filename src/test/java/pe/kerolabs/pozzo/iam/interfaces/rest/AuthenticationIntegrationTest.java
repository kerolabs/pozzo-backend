package pe.kerolabs.pozzo.iam.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.kerolabs.pozzo.support.MutableClock;
import pe.kerolabs.pozzo.support.PozzoApi;
import pe.kerolabs.pozzo.support.PozzoIntegrationTest;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@PozzoIntegrationTest
@DisplayName("Identity & Access: SMS sign-in, sessions and the protection of the API")
class AuthenticationIntegrationTest {

    @Autowired
    private PozzoApi api;

    @Autowired
    private MutableClock clock;

    @Test
    void aProtectedEndpointWithoutTokenAnswersWithTheStandardError() {
        var response = api.get("/api/v1/members/me/groups", null);

        assertThat(response.status()).isEqualTo(401);
        assertThat(response.text("$.code")).isEqualTo("AUTHENTICATION_REQUIRED");
    }

    @Test
    void aRegisteredMemberReadsTheirProfileWithTheSessionToken() {
        var anna = api.signUp("Anna Weber");

        var profile = api.get("/api/v1/members/me/profile", anna);

        assertThat(profile.status()).isEqualTo(200);
        assertThat(profile.text("$.displayName")).isEqualTo("Anna Weber");
        assertThat(profile.text("$.phoneNumber")).isEqualTo("+51" + anna.phoneNumber());
        assertThat(profile.text("$.accountId")).isEqualTo(anna.accountId());
    }

    @Test
    void verifyingTheCodeOfAnExistingAccountOpensASession() {
        var anna = api.signUp("Anna Weber");
        clock.advance(Duration.ofSeconds(31));
        api.post("/api/v1/auth/codes", null, Map.of("phoneNumber", anna.phoneNumber()));

        var verification = api.post("/api/v1/auth/codes/verify", null, Map.of("phoneNumber", anna.phoneNumber(),
                "code", api.lastCodeSentTo(anna.phoneNumber())));

        assertThat(verification.status()).isEqualTo(200);
        assertThat(verification.<Boolean>read("$.registrationRequired")).isFalse();
        assertThat(verification.text("$.session.token")).isNotBlank();
    }

    @Test
    void aWrongCodeIsRejectedAndTheThirdBlocksIt() {
        var phone = api.newPhoneNumber();
        api.post("/api/v1/auth/codes", null, Map.of("phoneNumber", phone));
        var wrong = Map.of("phoneNumber", phone, "code", "000000");

        var first = api.post("/api/v1/auth/codes/verify", null, wrong);
        api.post("/api/v1/auth/codes/verify", null, wrong);
        var third = api.post("/api/v1/auth/codes/verify", null, wrong);

        assertThat(first.status()).isEqualTo(401);
        assertThat(first.text("$.code")).isEqualTo("INVALID_VERIFICATION_CODE");
        assertThat(third.text("$.code")).isEqualTo("BLOCKED_VERIFICATION_CODE");
    }

    @Test
    void anotherCodeCannotBeRequestedBeforeThirtySeconds() {
        var phone = api.newPhoneNumber();
        api.post("/api/v1/auth/codes", null, Map.of("phoneNumber", phone));

        var again = api.post("/api/v1/auth/codes", null, Map.of("phoneNumber", phone));

        assertThat(again.status()).isEqualTo(429);
        assertThat(again.text("$.code")).isEqualTo("VERIFICATION_CODE_RESEND_TOO_SOON");
    }

    @Test
    void afterSigningOutTheTokenNoLongerWorks() {
        var anna = api.signUp("Anna Weber");

        assertThat(api.post("/api/v1/auth/sign-out", anna, null).status()).isEqualTo(204);
        assertThat(api.get("/api/v1/members/me/profile", anna).status()).isEqualTo(401);
    }
}
