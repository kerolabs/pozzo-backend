package pe.kerolabs.pozzo.contributions.domain.model.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A period opened: the members can contribute until its cutoff date.
 */
public record PeriodOpenedEvent(UUID periodId, UUID cycleId, UUID groupId, int turnNumber, UUID payoutMembershipId,
                                LocalDate cutoffDate, Instant occurredAt) {
}
