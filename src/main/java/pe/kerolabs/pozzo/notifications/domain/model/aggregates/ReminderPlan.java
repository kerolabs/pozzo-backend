package pe.kerolabs.pozzo.notifications.domain.model.aggregates;

import lombok.Getter;
import pe.kerolabs.pozzo.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ReminderPlan aggregate root: how many days before each cutoff date the members who have not paid
 * get a reminder, and at what time. There is one per group; by default three days, one day and the
 * same day, at 9:00.
 */
@Getter
public class ReminderPlan extends AbstractDomainAggregateRoot<ReminderPlan> {

    public static final List<Integer> DEFAULT_OFFSETS = List.of(3, 1, 0);
    public static final int DEFAULT_SEND_HOUR = 9;
    public static final int MAX_OFFSET = 30;

    private UUID id;
    private UUID groupId;
    private List<Integer> offsetsInDays = new ArrayList<>();
    private int sendHour;
    private boolean enabled;
    private Instant updatedAt;

    public ReminderPlan() {
    }

    public static ReminderPlan defaultFor(UUID groupId, Instant now) {
        var plan = new ReminderPlan();
        plan.id = UUID.randomUUID();
        plan.groupId = groupId;
        plan.offsetsInDays = new ArrayList<>(DEFAULT_OFFSETS);
        plan.sendHour = DEFAULT_SEND_HOUR;
        plan.enabled = true;
        plan.updatedAt = now;
        return plan;
    }

    /**
     * Changes the plan. Offsets are days before the cutoff (0 is the cutoff day itself), without repetitions.
     */
    public void configure(List<Integer> offsets, int sendHour, boolean enabled, Instant now) {
        if (offsets == null || offsets.isEmpty() || offsets.size() != offsets.stream().distinct().count()
                || offsets.stream().anyMatch(offset -> offset == null || offset < 0 || offset > MAX_OFFSET)) {
            throw new IllegalArgumentException(
                    "The reminders must be between 0 and %d days before the cutoff, without repetitions".formatted(MAX_OFFSET));
        }
        if (sendHour < 0 || sendHour > 23) {
            throw new IllegalArgumentException("The hour must be between 0 and 23");
        }
        this.offsetsInDays = new ArrayList<>(offsets.stream().sorted(Comparator.reverseOrder()).toList());
        this.sendHour = sendHour;
        this.enabled = enabled;
        this.updatedAt = now;
    }

    /**
     * The moments the reminders of a cutoff date go out, by offset, in the time zone of the group.
     */
    public Map<Integer, Instant> scheduleFor(LocalDate cutoffDate, ZoneId zone) {
        var schedule = new LinkedHashMap<Integer, Instant>();
        if (!enabled) {
            return schedule;
        }
        offsetsInDays.forEach(offset -> schedule.put(offset,
                cutoffDate.minusDays(offset).atTime(LocalTime.of(sendHour, 0)).atZone(zone).toInstant()));
        return schedule;
    }

    public List<Integer> getOffsetsInDays() {
        return Collections.unmodifiableList(offsetsInDays);
    }

    /**
     * Restores the aggregate from persistence.
     */
    public void restoreState(UUID id, UUID groupId, List<Integer> offsetsInDays, int sendHour, boolean enabled,
                             Instant updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.offsetsInDays = new ArrayList<>(offsetsInDays);
        this.sendHour = sendHour;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }
}
