package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * A receipt did not match and waits for the organizer ({@code REQUIRED}), or the organizer rejected it
 * ({@code REJECTED}). Notifications warns the organizer or the member; Compliance History records rejections.
 *
 * @param stage REQUIRED or REJECTED
 */
public record ContributionReviewIntegrationEvent(UUID contributionId, UUID cycleId, UUID groupId, String groupName,
                                                 UUID periodId, CycleMember member, UUID organizerAccountId,
                                                 String stage, Instant occurredAt) {

    public static final String REQUIRED = "REQUIRED";
    public static final String REJECTED = "REJECTED";
}
