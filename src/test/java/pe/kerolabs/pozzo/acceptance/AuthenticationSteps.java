package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.support.MutableClock;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS01, the SMS authentication service. */
public class AuthenticationSteps {

    private final PozzoApi api;
    private final MutableClock clock;
    private final ScenarioState state;

    public AuthenticationSteps(PozzoApi api, MutableClock clock, ScenarioState state) {
        this.api = api;
        this.clock = clock;
        this.state = state;
    }

    @Given("a phone number without an account")
    public void aPhoneNumberWithoutAnAccount() {
        state.keepPhoneNumber(api.newPhoneNumber(), null);
    }

    @When("the client requests a verification code for the number")
    public void requestsACode() {
        var requestedAt = clock.instant();
        state.keepResponse(api.post("/api/v1/auth/codes", null, Map.of("phoneNumber", state.phoneNumber())));
        state.keepPhoneNumber(state.phoneNumber(), requestedAt);
    }

    @Given("a phone number without an account received a verification code")
    public void aNewNumberReceivedACode() {
        aPhoneNumberWithoutAnAccount();
        requestsACode();
    }

    @Given("{string} received a new verification code")
    public void aMemberReceivedANewCode(String name) {
        state.keepPhoneNumber(state.member(name).phoneNumber(), null);
        requestsACode();
    }

    @Then("an SMS with a six-digit code reaches the number")
    public void anSmsWithACodeArrives() {
        assertThat(api.lastCodeSentTo(state.phoneNumber())).matches("\\d{6}");
    }

    @Then("the code expires {int} minutes after the request")
    public void theCodeExpires(int minutes) {
        assertThat(Instant.parse(state.response().text("$.expiresAt")))
                .isEqualTo(state.codeRequestedAt().plus(Duration.ofMinutes(minutes)));
    }

    @Then("another code can be requested {int} seconds after the request")
    public void anotherCodeCanBeRequested(int seconds) {
        assertThat(Instant.parse(state.response().text("$.resendAvailableAt")))
                .isEqualTo(state.codeRequestedAt().plusSeconds(seconds));
    }

    @When("the client verifies the number with the code it received")
    public void verifiesWithTheCodeReceived() {
        verifiesWithTheCode(api.lastCodeSentTo(state.phoneNumber()));
    }

    @When("the client verifies the number of {string} with the code it received")
    public void verifiesTheNumberOf(String name) {
        state.keepPhoneNumber(state.member(name).phoneNumber(), null);
        verifiesWithTheCodeReceived();
    }

    @When("the client verifies the number with the code {string}")
    public void verifiesWithTheCode(String code) {
        state.keepResponse(api.post("/api/v1/auth/codes/verify", null,
                Map.of("phoneNumber", state.phoneNumber(), "code", code, "deviceLabel", "Pixel 8")));
    }

    @Given("the client verified the number with the code {string} {int} times")
    public void verifiedSeveralTimes(String code, int times) {
        for (int i = 0; i < times; i++) {
            verifiesWithTheCode(code);
        }
    }

    @Then("the response asks to complete the registration with a registration token")
    public void asksToCompleteTheRegistration() {
        assertThat(state.response().<Boolean>read("$.registrationRequired")).isTrue();
        assertThat(state.response().text("$.registrationToken")).isNotBlank();
    }

    @Given("a phone number without an account was verified")
    public void aNewNumberWasVerified() {
        aNewNumberReceivedACode();
        verifiesWithTheCodeReceived();
        state.keepRegistrationToken(state.response().text("$.registrationToken"));
    }

    @When("the client completes the registration as {string} accepting the terms")
    public void completesTheRegistration(String name) {
        state.keepResponse(api.post("/api/v1/auth/register", null, Map.of(
                "registrationToken", state.registrationToken(),
                "displayName", name,
                "termsAccepted", true,
                "deviceLabel", "Pixel 8")));
    }

    @Then("the response contains a session token for {string}")
    public void containsASessionTokenFor(String name) {
        assertThat(state.response().text("$.token")).isNotBlank();
        assertThat(state.response().text("$.profile.displayName")).isEqualTo(name);
    }

    @Then("the response contains a session token")
    public void containsASessionToken() {
        assertThat(state.response().<Boolean>read("$.registrationRequired")).isFalse();
        assertThat(state.response().text("$.session.token")).isNotBlank();
    }
}
