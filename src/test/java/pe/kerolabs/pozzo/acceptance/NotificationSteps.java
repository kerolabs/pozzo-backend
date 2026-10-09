package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS07: devices, reminder plans and the notices sent by event. */
public class NotificationSteps {

    private final PozzoApi api;
    private final ScenarioState state;

    public NotificationSteps(PozzoApi api, ScenarioState state) {
        this.api = api;
        this.state = state;
    }

    @When("{string} registers a phone with the push token {string}")
    @Given("{string} registered a phone with the push token {string}")
    public void registersAPhone(String name, String pushToken) {
        state.keepResponse(api.post("/api/v1/members/me/devices", state.member(name),
                Map.of("pushToken", pushToken, "platform", "ANDROID")));
    }

    @Then("the device is active")
    public void theDeviceIsActive() {
        assertThat(state.response().<Boolean>read("$.active")).isTrue();
    }

    @When("{string} checks the reminders of the group")
    public void checksTheReminders(String name) {
        state.keepResponse(api.get("/api/v1/groups/" + state.groupId() + "/reminder-plan", state.member(name)));
    }

    @Then("the reminders are sent {int}, {int} and {int} days before the cutoff at {int}:00")
    public void theRemindersAreSent(int first, int second, int third, int hour) {
        assertThat(state.response().<List<Integer>>read("$.offsetsInDays")).containsExactly(first, second, third);
        assertThat(state.response().<Integer>read("$.sendHour")).isEqualTo(hour);
        assertThat(state.response().<Boolean>read("$.enabled")).isTrue();
    }

    @Then("{string} has the notice {string}")
    public void hasTheNotice(String name, String title) {
        assertThat(api.get("/api/v1/members/me/notifications", state.member(name)).<List<String>>read("$[*].title"))
                .contains(title);
    }
}
