package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS08: the compliance history and its public link. */
public class ComplianceSteps {

    private final PozzoApi api;
    private final ScenarioState state;

    public ComplianceSteps(PozzoApi api, ScenarioState state) {
        this.api = api;
        this.state = state;
    }

    @When("{string} checks the compliance history")
    public void checksTheHistory(String name) {
        state.keepResponse(api.get("/api/v1/members/me/compliance", state.member(name)));
    }

    @Then("the history has the level {string}, {int} % compliance and {int} contribution on time")
    public void theHistoryHas(String level, int rate, int onTime) {
        assertThat(state.response().text("$.summary.level")).isEqualTo(level);
        assertThat(state.response().<Integer>read("$.summary.complianceRate")).isEqualTo(rate);
        assertThat(state.response().<Integer>read("$.summary.onTime")).isEqualTo(onTime);
    }

    @Then("the history has the detail of the group")
    public void theHistoryHasTheGroup() {
        assertThat(state.response().<List<String>>read("$.groups[*].groupId")).contains(state.groupId());
    }

    @When("{string} checks the summary of {string}")
    public void checksTheSummaryOf(String name, String member) {
        state.keepResponse(api.get("/api/v1/members/" + state.member(member).accountId() + "/compliance/summary",
                state.member(name)));
    }

    @Then("the summary has {int} contribution on time")
    public void theSummaryHas(int onTime) {
        assertThat(state.response().<Integer>read("$.onTime")).isEqualTo(onTime);
    }

    @When("{string} checks the compliance of the group")
    public void checksTheGroupCompliance(String name) {
        state.keepResponse(api.get("/api/v1/groups/" + state.groupId() + "/compliance", state.member(name)));
    }

    @Then("the compliance lists {string} and {string}")
    public void theComplianceLists(String first, String second) {
        assertThat(state.response().<List<String>>read("$[*].displayName")).containsExactlyInAnyOrder(first, second);
    }

    @Given("{string} shared the history")
    public void sharedTheHistory(String name) {
        var link = api.post("/api/v1/members/me/compliance/share", state.member(name), null);
        assertThat(link.status()).isEqualTo(201);
        state.keepShareToken(link.text("$.token"));
    }

    @Given("{string} revoked the link")
    public void revokedTheLink(String name) {
        assertThat(api.delete("/api/v1/compliance/shares/" + state.shareToken(), state.member(name)).status())
                .isEqualTo(204);
    }

    @When("anyone opens the shared link without signing in")
    public void opensTheSharedLink() {
        state.keepResponse(api.get("/api/v1/compliance/shared/" + state.shareToken(), null));
    }

    @Then("the shared history shows {string} and the summary without amounts or group names")
    public void theSharedHistoryShows(String name) {
        assertThat(state.response().text("$.displayName")).isEqualTo(name);
        assertThat(state.response().text("$.summary.level")).isNotBlank();
        assertThat(state.response().body()).doesNotContain("Junta de la familia Weber").doesNotContain("amount");
    }
}
