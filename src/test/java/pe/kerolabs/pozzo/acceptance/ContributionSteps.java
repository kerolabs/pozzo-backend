package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.support.PozzoApi;
import pe.kerolabs.pozzo.support.PozzoApi.Response;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS05 and TS06: contributions, receipts, the pot and its delivery. */
public class ContributionSteps {

    private final PozzoApi api;
    private final ScenarioState state;

    public ContributionSteps(PozzoApi api, ScenarioState state) {
        this.api = api;
        this.state = state;
    }

    private Response currentPeriod() {
        return api.get("/api/v1/cycles/" + state.cycleId() + "/periods/current", state.organizer());
    }

    private String currentPeriodId() {
        return currentPeriod().text("$.periodId");
    }

    /** The membership of a member in the pot of the current period. */
    private String membershipInPot(String name) {
        List<String> ids = currentPeriod().read("$.members[?(@.displayName == '" + name + "')].membershipId");
        return ids.getFirst();
    }

    private String statusInPot(String name) {
        List<String> statuses = currentPeriod().read("$.members[?(@.displayName == '" + name + "')].status");
        return statuses.getFirst();
    }

    // TS05: contributions and receipts

    @When("{string} registers a receipt of S\\/ {int} paid to {string} with the operation {string}")
    @Given("{string} registered a receipt of S\\/ {int} paid to {string} with the operation {string}")
    public void registersAReceipt(String name, int amount, String payee, String operation) {
        var response = api.post("/api/v1/periods/" + currentPeriodId() + "/contributions", state.member(name),
                PozzoApi.receipt(operation, amount, payee));
        state.keepResponse(response);
        if (response.status() == 201) {
            state.keepContribution(name, response.text("$.id"));
        }
    }

    @Then("{string} can register a receipt of S\\/ {int} paid to {string} with the operation {string}")
    public void canRegisterAReceipt(String name, int amount, String payee, String operation) {
        registersAReceipt(name, amount, payee, operation);
        assertThat(state.response().status()).isEqualTo(201);
    }

    @Then("the contribution is in status {string} without inconsistencies")
    public void isValidatedWithoutInconsistencies(String status) {
        theContributionIsInStatus(status);
        assertThat(state.response().<List<Object>>read("$.inconsistencies")).isEmpty();
    }

    @Then("the contribution is in status {string}")
    public void theContributionIsInStatus(String status) {
        assertThat(state.response().text("$.status")).isEqualTo(status);
    }

    @Then("the field {string} did not match")
    public void theFieldDidNotMatch(String field) {
        assertThat(state.response().<List<String>>read("$.inconsistencies[*].field")).containsExactly(field);
    }

    @When("{string} approves the contribution of {string}")
    public void approves(String organizer, String member) {
        review(organizer, member, "APPROVE");
    }

    @When("{string} rejects the contribution of {string}")
    public void rejects(String organizer, String member) {
        review(organizer, member, "REJECT");
    }

    private void review(String organizer, String member, String decision) {
        state.keepResponse(api.patch("/api/v1/contributions/" + state.contributionOf(member) + "/review",
                state.member(organizer), Map.of("decision", decision, "note", "Checked with the bank statement")));
    }

    @Then("{string} appears as paid in the pot")
    public void appearsAsPaid(String name) {
        assertThat(statusInPot(name)).isEqualTo("VALIDATED");
    }

    @Then("{string} appears as covered in the pot")
    public void appearsAsCovered(String name) {
        assertThat(statusInPot(name)).isEqualTo("COVERED");
    }

    @When("{string} registers S\\/ {int} in cash for {string}")
    public void registersCash(String organizer, int amount, String member) {
        state.keepResponse(api.post("/api/v1/periods/" + currentPeriodId() + "/contributions/cash",
                state.member(organizer), Map.of("membershipId", membershipInPot(member), "amount", amount,
                        "receivedOn", LocalDate.now().toString())));
    }

    @Then("the contribution has the method {string} and the status {string}")
    public void hasTheMethodAndStatus(String method, String status) {
        assertThat(state.response().text("$.method")).isEqualTo(method);
        assertThat(state.response().text("$.status")).isEqualTo(status);
    }

    @When("{string} checks the state of the pot")
    public void checksThePot(String name) {
        state.keepResponse(api.get("/api/v1/cycles/" + state.cycleId() + "/periods/current", state.member(name)));
    }

    @Then("the pot has S\\/ {int} collected of S\\/ {int} and S\\/ {int} missing")
    public void thePotHas(int collected, int pot, int missing) {
        assertThat(state.response().<Double>read("$.collectedAmount")).isEqualTo((double) collected);
        assertThat(state.response().<Double>read("$.potAmount")).isEqualTo((double) pot);
        assertThat(state.response().<Double>read("$.missingAmount")).isEqualTo((double) missing);
    }

    @Then("the pot shows who collects, the days to the cutoff and the state of each member")
    public void thePotShowsTheDetail() {
        assertThat(state.response().text("$.payoutMemberName")).isNotBlank();
        assertThat(state.response().<Integer>read("$.daysToCutoff")).isNotNull();
        assertThat(state.response().<List<String>>read("$.members[*].status")).hasSize(2).doesNotContainNull();
    }

    // TS06: delivery of the pot and closing

    @Given("every member paid the current period")
    public void everyMemberPaid() {
        List<String> pending = currentPeriod().read("$.members[?(@.status == 'PENDING')].displayName");
        pending.forEach(name -> {
            registersCash(state.organizer().name(), 200, name);
            assertThat(state.response().status()).isEqualTo(201);
        });
    }

    @When("{string} confirms the delivery of the pot")
    @Given("{string} confirmed the delivery of the pot")
    public void confirmsTheDelivery(String organizer) {
        state.keepResponse(api.post("/api/v1/periods/" + currentPeriodId() + "/payout", state.member(organizer), null));
    }

    @Then("the period was delivered and turn {int} is open")
    public void thePeriodWasDelivered(int nextTurn) {
        assertThat(state.response().<Integer>read("$.deliveredTurn")).isEqualTo(nextTurn - 1);
        assertThat(state.response().<Integer>read("$.nextTurn")).isEqualTo(nextTurn);
        assertThat(currentPeriod().<Integer>read("$.turnNumber")).isEqualTo(nextTurn);
    }

    @Then("the cycle is {string}")
    public void theCycleIs(String status) {
        assertThat(state.response().text("$.cycleStatus")).isEqualTo(status);
    }

    @When("{string} covers the contribution of {string}")
    public void coversTheContribution(String organizer, String member) {
        state.keepResponse(api.post("/api/v1/periods/" + currentPeriodId() + "/contributions/coverage",
                state.member(organizer), Map.of(
                        "membershipId", membershipInPot(member),
                        "coveredByMembershipId", membershipInPot(organizer))));
    }

    @Then("the history of {string} counts {int} covered contribution")
    public void theHistoryCountsCovered(String name, int covered) {
        assertThat(api.get("/api/v1/members/me/compliance", state.member(name)).<Integer>read("$.summary.covered"))
                .isEqualTo(covered);
    }
}
