package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A period opened: every member owes the contribution until the cutoff date.
 * Notifications schedules the reminders of the members.
 */
public record PeriodOpenedIntegrationEvent(UUID periodId, UUID cycleId, UUID groupId, String groupName, int turnNumber,
                                           int totalTurns, LocalDate cutoffDate, BigDecimal contributionAmount,
                                           CycleMember payout, UUID organizerAccountId, List<CycleMember> members,
                                           Instant occurredAt) {
}
