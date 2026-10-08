package pe.kerolabs.pozzo.notifications.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Command to schedule the reminders of a period for the members who have to contribute.
 */
public record ScheduleRemindersCommand(UUID groupId, String groupName, UUID periodId, LocalDate cutoffDate,
                                       BigDecimal contributionAmount, List<UUID> accountIds) {
}
