package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import pe.kerolabs.pozzo.support.MutableClock;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps every Technical Story uses: the accounts, the passing of time and the response of the service.
 */
public class CommonSteps {

    private final PozzoApi api;
    private final MutableClock clock;
    private final ScenarioState state;

    public CommonSteps(PozzoApi api, MutableClock clock, ScenarioState state) {
        this.api = api;
        this.clock = clock;
        this.state = state;
    }

    @Given("{string} has a Pozzo account")
    public void hasAnAccount(String name) {
        state.add(api.signUp(name));
    }

    @Given("{int} seconds have passed")
    public void secondsHavePassed(int seconds) {
        clock.advance(Duration.ofSeconds(seconds));
    }

    @Given("{int} minutes have passed")
    public void minutesHavePassed(int minutes) {
        clock.advance(Duration.ofMinutes(minutes));
    }

    @Then("^the service responds (\\d{3}) [A-Za-z ]+$")
    public void theServiceResponds(int status) {
        assertThat(state.response().status())
                .as("status of the response %s", state.response().body())
                .isEqualTo(status);
    }

    @Then("the error code is {string}")
    public void theErrorCodeIs(String code) {
        assertThat(state.response().text("$.code")).isEqualTo(code);
    }

    @Then("the error says {string}")
    public void theErrorSays(String details) {
        assertThat(state.response().text("$.details")).contains(details);
    }
}
