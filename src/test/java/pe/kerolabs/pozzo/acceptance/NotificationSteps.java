package pe.kerolabs.pozzo.acceptance;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pe.kerolabs.pozzo.notifications.application.commandservices.NotificationCommandService;
import pe.kerolabs.pozzo.notifications.domain.model.commands.DispatchDueNotificationsCommand;
import pe.kerolabs.pozzo.notifications.domain.repositories.NotificationRepository;
import pe.kerolabs.pozzo.shared.application.result.Result;
import pe.kerolabs.pozzo.support.MutableClock;
import pe.kerolabs.pozzo.support.PozzoApi;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Steps of TS07: devices, reminder plans and the notices sent by event. */
public class NotificationSteps {

    private static final int DISPATCH_BATCH = 500;

    private final PozzoApi api;
    private final ScenarioState state;
    private final MutableClock clock;
    private final NotificationCommandService notificationCommandService;
    private final NotificationRepository notificationRepository;

    public NotificationSteps(PozzoApi api, ScenarioState state, MutableClock clock,
                             NotificationCommandService notificationCommandService,
                             NotificationRepository notificationRepository) {
        this.api = api;
        this.state = state;
        this.clock = clock;
        this.notificationCommandService = notificationCommandService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Moves the clock to noon of a day after the cutoff of the current period, sends what is due by then and
     * moves the clock back, so the next scenarios still start today.
     */
    @When("{int} days pass after the cutoff and the due reminders go out")
    public void daysPassAfterTheCutoff(int days) {
        var period = api.get("/api/v1/cycles/" + state.cycleId() + "/periods/current", state.organizer());
        var moment = LocalDate.parse(period.text("$.cutoffDate")).plusDays(days).atTime(12, 0)
                .atZone(ZoneId.of("America/Lima")).toInstant();
        var shift = Duration.between(clock.instant(), moment);
        clock.advance(shift);
        try {
            var sent = DISPATCH_BATCH;
            while (sent == DISPATCH_BATCH) {
                var result = notificationCommandService.handle(new DispatchDueNotificationsCommand(DISPATCH_BATCH));
                sent = result instanceof Result.Success<Integer, ?> success ? success.value() : 0;
            }
        } finally {
            clock.advance(shift.negated());
        }
    }

    @When("{string} turns the reminders of the group off")
    public void turnsTheRemindersOff(String name) {
        state.keepResponse(api.put("/api/v1/groups/" + state.groupId() + "/reminder-plan", state.member(name),
                Map.of("offsetsInDays", List.of(3, 1, 0), "sendHour", 9, "enabled", false)));
    }

    @Given("a reminder is waiting for {string} in the group")
    public void aReminderIsWaiting(String name) {
        var accountId = UUID.fromString(state.member(name).accountId());
        assertThat(notificationRepository.findScheduledRemindersByGroupId(UUID.fromString(state.groupId())))
                .anyMatch(reminder -> reminder.getAccountId().equals(accountId));
    }

    @Then("no reminder is waiting for {string} in the group")
    public void noReminderIsWaiting(String name) {
        var accountId = UUID.fromString(state.member(name).accountId());
        assertThat(notificationRepository.findScheduledRemindersByGroupId(UUID.fromString(state.groupId())))
                .noneMatch(reminder -> reminder.getAccountId().equals(accountId));
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

    @Then("{string} does not have the notice {string}")
    public void doesNotHaveTheNotice(String name, String title) {
        assertThat(api.get("/api/v1/members/me/notifications", state.member(name)).<List<String>>read("$[*].title"))
                .doesNotContain(title);
    }
}
