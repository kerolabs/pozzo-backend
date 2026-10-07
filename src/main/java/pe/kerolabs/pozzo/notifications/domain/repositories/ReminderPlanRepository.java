package pe.kerolabs.pozzo.notifications.domain.repositories;

import pe.kerolabs.pozzo.notifications.domain.model.aggregates.ReminderPlan;

import java.util.Optional;
import java.util.UUID;

/**
 * Reminder plan repository port.
 */
public interface ReminderPlanRepository {

    Optional<ReminderPlan> findByGroupId(UUID groupId);

    ReminderPlan save(ReminderPlan plan);
}
