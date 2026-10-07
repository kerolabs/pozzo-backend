package pe.kerolabs.pozzo.contributions.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The pot of a period is complete ({@code COMPLETED}) or was delivered ({@code DELIVERED}).
 * Notifications warns the organizer or the member who collects.
 *
 * @param stage COMPLETED or DELIVERED
 */
public record PotIntegrationEvent(UUID periodId, UUID cycleId, UUID groupId, String groupName, int turnNumber,
                                  CycleMember payout, UUID organizerAccountId, BigDecimal potAmount, String stage,
                                  Instant occurredAt) {

    public static final String COMPLETED = "COMPLETED";
    public static final String DELIVERED = "DELIVERED";
}
