package pe.kerolabs.pozzo.notifications.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReminderPlan: when the members of a group are reminded to pay")
class ReminderPlanTest {

    private static final Instant NOW = Instant.parse("2026-10-08T15:00:00Z");
    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Test
    void remindsThreeDaysBeforeOneDayBeforeAndOnTheCutoffAtNine() {
        var plan = ReminderPlan.defaultFor(UUID.randomUUID(), NOW);

        var schedule = plan.scheduleFor(LocalDate.parse("2026-10-20"), LIMA);

        assertThat(schedule).containsExactly(
                Map.entry(3, Instant.parse("2026-10-17T14:00:00Z")),
                Map.entry(1, Instant.parse("2026-10-19T14:00:00Z")),
                Map.entry(0, Instant.parse("2026-10-20T14:00:00Z")));
    }

    @Test
    void theOrganizerChoosesTheDaysAndTheHour() {
        var plan = ReminderPlan.defaultFor(UUID.randomUUID(), NOW);

        plan.configure(List.of(0, 5), 18, true, NOW);

        assertThat(plan.getOffsetsInDays()).containsExactly(5, 0);
        assertThat(plan.getSendHour()).isEqualTo(18);
    }

    @Test
    void aDisabledPlanSchedulesNothing() {
        var plan = ReminderPlan.defaultFor(UUID.randomUUID(), NOW);

        plan.configure(List.of(1), 9, false, NOW);

        assertThat(plan.scheduleFor(LocalDate.parse("2026-10-20"), LIMA)).isEmpty();
    }

    @Test
    void rejectsRepeatedDaysDaysOutOfRangeAndInvalidHours() {
        var plan = ReminderPlan.defaultFor(UUID.randomUUID(), NOW);

        assertThatThrownBy(() -> plan.configure(List.of(1, 1), 9, true, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plan.configure(List.of(31), 9, true, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plan.configure(List.of(), 9, true, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> plan.configure(List.of(1), 24, true, NOW)).isInstanceOf(IllegalArgumentException.class);
    }
}
