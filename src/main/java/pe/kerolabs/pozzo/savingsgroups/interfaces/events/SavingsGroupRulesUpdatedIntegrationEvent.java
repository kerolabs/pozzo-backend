package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration event that Savings Groups publishes when the organizer changes the rules of a group that has
 * not started.
 *
 * @param groupId            the savings group
 * @param groupName          the name of the group, after the change
 * @param memberAccountIds   the accounts of the members who use the application, without the organizer
 * @param contributionAmount the amount each member contributes every period
 * @param periodicity        WEEKLY, BIWEEKLY or MONTHLY
 * @param seats              the number of members
 * @param updatedAt          the moment of the change
 */
public record SavingsGroupRulesUpdatedIntegrationEvent(UUID groupId, String groupName, List<UUID> memberAccountIds,
                                                       BigDecimal contributionAmount, String periodicity, int seats,
                                                       Instant updatedAt) {
}
