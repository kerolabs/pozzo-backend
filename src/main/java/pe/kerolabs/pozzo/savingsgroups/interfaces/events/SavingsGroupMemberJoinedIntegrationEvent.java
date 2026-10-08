package pe.kerolabs.pozzo.savingsgroups.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event that Savings Groups publishes when a member joins a group with an invitation.
 *
 * @param groupId            the savings group
 * @param groupName          the name of the group
 * @param organizerAccountId the account of the organizer
 * @param membershipId       the membership of the new member
 * @param memberName         the name of the new member
 * @param membersCount       the active members after the join
 * @param seats              the seats of the group
 * @param joinedAt           the moment the member joined
 */
public record SavingsGroupMemberJoinedIntegrationEvent(UUID groupId, String groupName, UUID organizerAccountId,
                                                       UUID membershipId, String memberName, int membersCount,
                                                       int seats, Instant joinedAt) {
}
