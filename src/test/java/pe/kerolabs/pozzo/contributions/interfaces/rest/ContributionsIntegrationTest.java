package pe.kerolabs.pozzo.contributions.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pe.kerolabs.pozzo.support.PozzoApi;
import pe.kerolabs.pozzo.support.PozzoIntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.kerolabs.pozzo.support.PozzoApi.receipt;

/**
 * A contribution crosses three bounded contexts: Contributions validates it, and its events reach
 * Compliance History and Notifications after the transaction commits.
 */
@PozzoIntegrationTest
@DisplayName("Contributions: validation stored in PostgreSQL and its effect on the other contexts")
class ContributionsIntegrationTest {

    @Autowired
    private PozzoApi api;

    @Test
    void aValidatedContributionIsCollectedInThePot() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var group = api.startedGroup(anna, sofia);

        var registered = api.post("/api/v1/periods/" + group.periodId() + "/contributions", sofia,
                receipt("04581273", 200, "Anna Weber"));

        assertThat(registered.status()).isEqualTo(201);
        assertThat(registered.text("$.status")).isEqualTo("VALIDATED");
        var period = api.get("/api/v1/cycles/" + group.cycleId() + "/periods/current", anna);
        assertThat(period.<Double>read("$.collectedAmount")).isEqualTo(200.0);
        assertThat(period.<Integer>read("$.settledCount")).isEqualTo(1);
    }

    @Test
    void aReceiptCannotBeUsedTwiceInTheSameGroup() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var group = api.startedGroup(anna, sofia);
        var path = "/api/v1/periods/" + group.periodId() + "/contributions";
        api.post(path, sofia, receipt("04581273", 200, "Anna Weber"));

        var again = api.post(path, anna, receipt("04581273", 200, "Anna Weber"));

        assertThat(again.status()).isEqualTo(409);
        assertThat(again.text("$.code")).isEqualTo("RECEIPT_CONFLICT");
    }

    @Test
    void aValidatedContributionEntersTheComplianceHistoryOfTheMember() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var group = api.startedGroup(anna, sofia);

        api.post("/api/v1/periods/" + group.periodId() + "/contributions", sofia, receipt("04581273", 200, "Anna Weber"));

        var history = api.get("/api/v1/members/me/compliance", sofia);
        assertThat(history.<Integer>read("$.summary.onTime")).isEqualTo(1);
        assertThat(history.<Integer>read("$.summary.complianceRate")).isEqualTo(100);
    }

    @Test
    void anInconsistentReceiptNotifiesTheOrganizer() {
        var anna = api.signUp("Anna Weber");
        var sofia = api.signUp("Sofia Gonzales");
        var group = api.startedGroup(anna, sofia);

        var registered = api.post("/api/v1/periods/" + group.periodId() + "/contributions", sofia,
                receipt("07719346", 150, "Anna Weber"));

        assertThat(registered.text("$.status")).isEqualTo("INCONSISTENT");
        assertThat(api.get("/api/v1/periods/" + group.periodId() + "/contributions/pending-review", anna)
                .<List<String>>read("$[*].memberName")).containsExactly("Sofia Gonzales");
        assertThat(api.get("/api/v1/members/me/notifications", anna).<List<String>>read("$[*].body"))
                .anyMatch(body -> body.contains("Sofia Gonzales"));
    }
}
