package pe.kerolabs.pozzo.savingsgroups.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.kerolabs.pozzo.support.PozzoApi;
import pe.kerolabs.pozzo.support.PozzoIntegrationTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@PozzoIntegrationTest
@DisplayName("Savings Groups: groups, invitations and turns stored in PostgreSQL")
class SavingsGroupsIntegrationTest {

    @Autowired
    private PozzoApi api;

    @Test
    void aCreatedGroupIsStoredAndListedForItsOrganizer() {
        var anna = api.signUp("Anna Weber");

        var groupId = api.createGroup(anna, 4);

        var group = api.get("/api/v1/groups/" + groupId, anna);
        assertThat(group.status()).isEqualTo(200);
        assertThat(group.text("$.status")).isEqualTo("DRAFT");
        assertThat(group.text("$.role")).isEqualTo("ORGANIZER");
        assertThat(group.<Integer>read("$.freeSeats")).isEqualTo(3);
        assertThat(api.get("/api/v1/members/me/groups", anna).<List<String>>read("$[*].id")).contains(groupId);
    }

    @Test
    void aMemberJoinsWithTheInvitationCodeAndAppearsInTheMemberList() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var groupId = api.createGroup(anna, 3);
        var code = api.invitationCode(anna, groupId);

        var preview = api.get("/api/v1/invitations/" + code, sofia);
        var joined = api.post("/api/v1/invitations/" + code + "/join", sofia, null);

        assertThat(preview.<Integer>read("$.freeSeats")).isEqualTo(2);
        assertThat(joined.status()).isEqualTo(200);
        var members = api.get("/api/v1/groups/" + groupId + "/members", anna);
        assertThat(members.<List<String>>read("$[*].displayName")).containsExactly("Anna Weber", "Sofia Gonzales");
        assertThat(api.get("/api/v1/members/me/groups", sofia).<List<String>>read("$[*].role")).containsExactly("PARTICIPANT");
    }

    @Test
    void someoneOutsideTheGroupCannotSeeIt() {
        var anna = api.signUp("Anna Weber");
        var stranger = api.signUp("Carla Vega");
        var groupId = api.createGroup(anna, 3);

        var response = api.get("/api/v1/groups/" + groupId, stranger);

        assertThat(response.status()).isEqualTo(404);
        assertThat(response.text("$.code")).isEqualTo("SAVINGS_GROUP_NOT_FOUND");
    }

    @Test
    void theSeatsCannotBeLoweredBelowTheMembersAlreadyIn() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var jorge = api.signUp("Jorge Ramos");
        var groupId = api.createGroup(anna, 4);
        var code = api.invitationCode(anna, groupId);
        api.post("/api/v1/invitations/" + code + "/join", sofia, null);
        api.post("/api/v1/invitations/" + code + "/join", jorge, null);

        var response = api.put("/api/v1/groups/" + groupId + "/rules", anna, Map.of(
                "name", "Junta de la familia Weber", "contributionAmount", 200.0, "periodicity", "MONTHLY",
                "seats", 2, "firstContributionDate", PozzoApi.FIRST_CUTOFF.toString()));

        assertThat(response.status()).isEqualTo(422);
        assertThat(response.text("$.code")).isEqualTo("SEATS_BELOW_MEMBERS");
    }

    @Test
    void startingTheGroupFreezesItAndOpensTheFirstPeriod() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");

        var started = api.startedGroup(anna, sofia);

        assertThat(api.get("/api/v1/groups/" + started.groupId(), anna).text("$.status")).isEqualTo("STARTED");
        var period = api.get("/api/v1/cycles/" + started.cycleId() + "/periods/current", sofia);
        assertThat(period.<Integer>read("$.turnNumber")).isEqualTo(1);
        assertThat(period.<Double>read("$.potAmount")).isEqualTo(400.0);
        assertThat(period.text("$.cutoffDate")).isEqualTo(PozzoApi.FIRST_CUTOFF.toString());
    }
}
