package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A member settled what they owed in a period: on time, late, or covered by another member.
 * Compliance History records it and Notifications stops the reminders of that member.
 *
 * @param outcome ON_TIME, LATE or COVERED
 */
public record ContributionSettledIntegrationEvent(UUID contributionId, UUID cycleId, UUID groupId, String groupName,
                                                  UUID periodId, int turnNumber, CycleMember member, String outcome,
                                                  BigDecimal amount, Instant occurredAt) {

    public static final String ON_TIME = "ON_TIME";
    public static final String LATE = "LATE";
    public static final String COVERED = "COVERED";
}
