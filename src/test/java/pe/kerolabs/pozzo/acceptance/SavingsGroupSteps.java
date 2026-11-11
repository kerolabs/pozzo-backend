package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS02, TS03 and TS04: savings groups, their members, invitations and turns. */
public class SavingsGroupSteps {

    private final PozzoApi api;
    private final ScenarioState state;

    /** The number the organizer gave a member registered without the application in this scenario. */
    private String manualMemberPhone;

    /** The membership of the member who dropped out in this scenario. */
    private String droppedMembershipId;

    public SavingsGroupSteps(PozzoApi api, ScenarioState state) {
        this.api = api;
        this.state = state;
    }

    private String groupPath() {
        return "/api/v1/groups/" + state.groupId();
    }

    /** The id of the membership of a member, read from the member list the organizer sees. */
    private String membershipOf(String name) {
        List<Map<String, Object>> members = api.get(groupPath() + "/members", state.organizer()).read("$");
        return members.stream().filter(member -> name.equals(member.get("displayName")))
                .map(member -> String.valueOf(member.get("id")))
                .findFirst().orElseThrow(() -> new IllegalStateException(name + " is not in the group"));
    }

    // TS02: savings groups

    @When("{string} creates a monthly group {string} of S\\/ {int} with {int} seats and Yape as destination")
    public void createsAGroup(String organizer, String name, int amount, int seats) {
        var member = state.member(organizer);
        var response = api.post("/api/v1/groups", member, Map.of(
                "name", name,
                "contributionAmount", amount,
                "periodicity", "MONTHLY",
                "seats", seats,
                "firstContributionDate", PozzoApi.FIRST_CUTOFF.toString(),
                "destination", Map.of("method", "YAPE", "phoneNumber", member.phoneNumber())));
        state.keepResponse(response);
        if (response.status() == 201) {
            state.keepGroup(organizer, response.text("$.id"), null);
        }
    }

    @Then("the group is in status {string}")
    public void theGroupIsInStatus(String status) {
        assertThat(api.get(groupPath(), state.organizer()).text("$.status")).isEqualTo(status);
    }

    @Then("{string} is its organizer")
    public void isItsOrganizer(String name) {
        var group = api.get(groupPath(), state.member(name));
        assertThat(group.text("$.organizerName")).isEqualTo(name);
        assertThat(group.text("$.role")).isEqualTo("ORGANIZER");
    }

    @Given("{string} has a group of {int} seats")
    public void hasAGroup(String organizer, int seats) {
        state.keepGroup(organizer, api.createGroup(state.member(organizer), seats), null);
    }

    @Given("{string} has a group of {int} seats with an invitation")
    public void hasAGroupWithAnInvitation(String organizer, int seats) {
        var member = state.member(organizer);
        var groupId = api.createGroup(member, seats);
        state.keepGroup(organizer, groupId, api.invitationCode(member, groupId));
    }

    @Given("{string} has a group of {int} seats where {string} joined")
    public void hasAGroupWhereOneJoined(String organizer, int seats, String member) {
        hasAGroupWithAnInvitation(organizer, seats);
        joinsWithTheInvitation(member);
    }

    @Given("{string} has a group of {int} seats where {string} and {string} joined")
    public void hasAGroupWhereTwoJoined(String organizer, int seats, String first, String second) {
        hasAGroupWhereOneJoined(organizer, seats, first);
        joinsWithTheInvitation(second);
    }

    @Given("{string} started a group with {string}")
    @Given("{string} started a group with {string} that sends the contributions to Yape")
    public void startedAGroup(String organizer, String member) {
        var group = api.startedGroup(state.member(organizer), state.member(member));
        state.keepGroup(organizer, group.groupId(), group.invitationCode());
        state.keepCycle(group.cycleId());
    }

    /** Opens the next cycle and, when it is created, follows the new group in the next steps. */
    @When("{string} opens a new cycle of the group")
    public void opensANewCycle(String organizer) {
        state.keepResponse(api.post(groupPath() + "/new-cycle", state.member(organizer),
                Map.of("firstContributionDate", PozzoApi.FIRST_CUTOFF.plusMonths(3).toString())));
        if (state.response().status() == 201) {
            state.keepGroup(organizer, state.response().text("$.id"), null);
        }
    }

    @When("{string} registers that {string} dropped out")
    @Given("{string} registered that {string} dropped out")
    public void registersADropout(String organizer, String name) {
        droppedMembershipId = membershipOf(name);
        state.keepResponse(api.post(groupPath() + "/members/" + droppedMembershipId + "/dropout",
                state.member(organizer), null));
    }

    @When("{string} replaces the member who dropped out with {string}")
    public void replacesWithAManualMember(String organizer, String name) {
        state.keepResponse(api.post(groupPath() + "/members/" + droppedMembershipId + "/replacement",
                state.member(organizer), Map.of("displayName", name)));
    }

    @When("{string} replaces the member who dropped out with the number of {string}")
    public void replacesWithAnAccount(String organizer, String name) {
        state.keepResponse(api.post(groupPath() + "/members/" + droppedMembershipId + "/replacement",
                state.member(organizer), Map.of("displayName", "Otro nombre",
                        "phoneNumber", state.member(name).phoneNumber())));
    }

    @When("{string} lists the groups")
    public void listsTheGroups(String name) {
        state.keepResponse(api.get("/api/v1/members/me/groups", state.member(name)));
    }

    @Then("the list has the group with the role {string} and a turn")
    public void theListHasTheGroup(String role) {
        List<Map<String, Object>> groups = state.response().read("$[?(@.id == '" + state.groupId() + "')]");
        assertThat(groups).singleElement().satisfies(group -> {
            assertThat(group.get("role")).isEqualTo(role);
            assertThat(group.get("myTurnNumber")).isNotNull();
        });
    }

    @When("{string} starts the group")
    public void startsTheGroup(String organizer) {
        var started = api.post(groupPath() + "/start", state.member(organizer), null);
        state.keepResponse(started);
        if (started.status() == 200) {
            state.keepCycle(api.get(groupPath() + "/cycle", state.member(organizer)).text("$.id"));
        }
    }

    @Then("the cycle of the group is open on turn {int}")
    public void theCycleIsOpen(int turn) {
        var cycle = api.get(groupPath() + "/cycle", state.organizer());
        assertThat(cycle.text("$.status")).isEqualTo("ACTIVE");
        assertThat(cycle.<Integer>read("$.currentTurn")).isEqualTo(turn);
    }

    @When("{string} changes the contribution of the group to S\\/ {int}")
    public void changesTheContribution(String organizer, int amount) {
        var group = api.get(groupPath(), state.member(organizer));
        state.keepResponse(api.put(groupPath() + "/rules", state.member(organizer), Map.of(
                "name", group.text("$.name"),
                "contributionAmount", amount,
                "periodicity", group.text("$.rules.periodicity"),
                "seats", group.<Integer>read("$.rules.seats"),
                "firstContributionDate", group.text("$.rules.firstContributionDate"))));
    }

    // TS03: members and invitations

    @When("{string} opens the invitation")
    public void opensTheInvitation(String name) {
        opensTheInvitationCode(name, state.invitationCode());
    }

    @When("{string} opens the invitation {string}")
    public void opensTheInvitationCode(String name, String code) {
        state.keepResponse(api.get("/api/v1/invitations/" + code, state.member(name)));
    }

    @Then("the preview shows the name, the rules and {int} free seats")
    public void thePreviewShowsTheGroup(int freeSeats) {
        var preview = state.response();
        assertThat(preview.text("$.name")).isEqualTo("Junta de la familia Weber");
        assertThat(preview.<Double>read("$.rules.contributionAmount")).isEqualTo(200.0);
        assertThat(preview.<Integer>read("$.freeSeats")).isEqualTo(freeSeats);
    }

    @Then("the preview does not show the members or the destination")
    public void thePreviewHidesMembersAndDestination() {
        Map<String, Object> preview = state.response().read("$");
        Map<String, Object> rules = state.response().read("$.rules");
        assertThat(preview).doesNotContainKeys("members", "memberships");
        assertThat(rules.get("destination")).isNull();
    }

    @When("{string} joins with the invitation")
    public void joinsWithTheInvitation(String name) {
        state.keepResponse(api.post("/api/v1/invitations/" + state.invitationCode() + "/join", state.member(name), null));
    }

    @Then("{string} is a member of the group")
    public void isAMember(String name) {
        assertThat(api.get(groupPath() + "/members", state.organizer()).<List<String>>read("$[*].displayName"))
                .contains(name);
    }

    @Then("{string} is no longer a member of the group")
    public void isNoLongerAMember(String name) {
        assertThat(api.get(groupPath() + "/members", state.organizer()).<List<String>>read("$[*].displayName"))
                .doesNotContain(name);
    }

    @When("{string} registers {string} without the application with the number {int}")
    public void registersAManualMember(String organizer, String name, int phone) {
        state.keepResponse(api.post(groupPath() + "/members/manual", state.member(organizer),
                Map.of("displayName", name, "phoneNumber", String.valueOf(phone))));
    }

    @Given("{string} registered {string} without the application with a number")
    public void registeredAManualMemberWithANumber(String organizer, String name) {
        manualMemberPhone = api.newPhoneNumber();
        var added = api.post(groupPath() + "/members/manual", state.member(organizer),
                Map.of("displayName", name, "phoneNumber", manualMemberPhone));
        assertThat(added.status()).as(added.body()).isEqualTo(201);
    }

    @Given("{string} started a group with {string} registered without the application")
    public void startedAGroupWithAManualMember(String organizer, String name) {
        var groupId = api.createGroup(state.member(organizer), 2);
        state.keepGroup(organizer, groupId, null);
        registeredAManualMemberWithANumber(organizer, name);
        api.post(groupPath() + "/turns/draw", state.member(organizer), null);
        var started = api.post(groupPath() + "/start", state.member(organizer), null);
        assertThat(started.status()).as(started.body()).isEqualTo(200);
        state.keepCycle(api.get(groupPath() + "/cycle", state.member(organizer)).text("$.id"));
    }

    @When("{string} signs up with the number the organizer registered")
    public void signsUpWithTheRegisteredNumber(String name) {
        state.add(api.signUp(name, manualMemberPhone));
    }

    @Then("the group is among the groups of {string}")
    public void theGroupIsAmongTheGroupsOf(String name) {
        assertThat(api.get("/api/v1/members/me/groups", state.member(name)).<List<String>>read("$[*].id"))
                .contains(state.groupId());
    }

    @Then("{string} is a member of kind {string}")
    public void isAMemberOfKind(String name, String kind) {
        List<String> kinds = api.get(groupPath() + "/members", state.organizer())
                .read("$[?(@.displayName == '" + name + "')].kind");
        assertThat(kinds).containsExactly(kind);
    }

    @When("{string} removes {string} from the group")
    @Given("{string} removed {string} from the group")
    public void removesAMember(String organizer, String name) {
        state.keepResponse(api.delete(groupPath() + "/members/" + membershipOf(name), state.member(organizer)));
    }

    // TS04: turns

    @When("{string} draws the turns")
    @Given("{string} drew the turns")
    public void drawsTheTurns(String organizer) {
        state.keepResponse(api.post(groupPath() + "/turns/draw", state.member(organizer), null));
    }

    @Then("every member has one turn from {int} to {int} with its cutoff date")
    public void everyMemberHasOneTurn(int first, int last) {
        List<Integer> numbers = state.response().read("$.turns[*].turnNumber");
        List<String> members = state.response().read("$.turns[*].membershipId");
        List<String> cutoffs = state.response().read("$.turns[*].cutoffDate");
        assertThat(numbers).containsExactlyElementsOf(IntStream.rangeClosed(first, last).boxed().toList());
        assertThat(members).doesNotHaveDuplicates().hasSize(last);
        assertThat(cutoffs).hasSize(last).doesNotContainNull();
    }

    @Then("the calendar shows the seed of the draw")
    public void theCalendarShowsTheSeed() {
        assertThat(state.response().text("$.method")).isEqualTo("DRAW");
        assertThat(state.response().text("$.drawSeed")).isNotBlank();
    }

    @When("{string} sets the order {string}, {string}, {string}")
    public void setsTheOrder(String organizer, String first, String second, String third) {
        var order = List.of(membershipOf(first), membershipOf(second), membershipOf(third));
        state.keepResponse(api.post(groupPath() + "/turns/agreed", state.member(organizer), Map.of("order", order)));
    }

    @When("{string} swaps the turns of {string} and {string}")
    public void swapsTheTurns(String name, String first, String second) {
        var cycleId = api.get(groupPath() + "/cycle", state.organizer()).text("$.id");
        state.keepResponse(api.post("/api/v1/cycles/" + cycleId + "/turns/swap", state.member(name), Map.of(
                "firstMembershipId", membershipOf(first), "secondMembershipId", membershipOf(second))));
    }

    @When("{string} auctions the turns with a minimum bid of S\\/ {int} and a draw when nobody bids")
    public void auctionsTheTurns(String organizer, int minimumBid) {
        state.keepResponse(api.post(groupPath() + "/turns/auction", state.member(organizer), Map.of(
                "closesDaysBeforeCutoff", 2, "minimumBid", minimumBid, "fallback", "DRAW")));
    }

    @Then("the turns follow the order {string}, {string}, {string}")
    public void theTurnsFollowTheOrder(String first, String second, String third) {
        assertThat(state.response().<List<String>>read("$.turns[*].displayName")).containsExactly(first, second, third);
        assertThat(state.response().text("$.method")).isEqualTo("AGREED");
    }

    @Then("the calendar of the group follows the order {string}, {string}, {string}")
    public void theCalendarFollowsTheOrder(String first, String second, String third) {
        assertThat(api.get(groupPath() + "/turns", state.organizer()).<List<String>>read("$.turns[*].displayName"))
                .containsExactly(first, second, third);
    }
}
