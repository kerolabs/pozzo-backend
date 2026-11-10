package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A member who took part in a cycle without the application linked the account they signed up with.
 * Compliance History records for that account the contributions settled so far and, when the cycle is
 * over, the completed cycle.
 *
 * @param settled     the contributions of the member that count for the pot, oldest first
 * @param cycleClosed whether the cycle had already ended
 */
public record CycleMemberLinkedIntegrationEvent(UUID cycleId, UUID groupId, String groupName, UUID membershipId,
                                                UUID accountId, List<SettledContribution> settled,
                                                boolean cycleClosed, Instant occurredAt) {

    /**
     * A settled contribution of the member.
     *
     * @param outcome   ON_TIME, LATE or COVERED, as in {@link ContributionSettledIntegrationEvent}
     * @param settledAt when it was registered
     */
    public record SettledContribution(UUID contributionId, UUID periodId, String outcome, Instant settledAt) {
    }
}
