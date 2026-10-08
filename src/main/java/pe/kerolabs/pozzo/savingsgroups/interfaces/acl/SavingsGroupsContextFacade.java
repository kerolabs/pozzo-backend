package pe.kerolabs.pozzo.savingsgroups.interfaces.acl;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * What Savings Groups offers to the other bounded contexts, so they can authorize requests about a
 * group without reading the Savings Groups model.
 */
public interface SavingsGroupsContextFacade {

    /**
     * Returns true when the account is the organizer of the group.
     */
    boolean isOrganizer(UUID groupId, UUID accountId);

    /**
     * Returns true when the account has an active membership in the group.
     */
    boolean isMember(UUID groupId, UUID accountId);

    /**
     * Returns true when the organizer runs a group where the other account is an active member.
     */
    boolean isOrganizerOfMember(UUID organizerAccountId, UUID memberAccountId);

    /**
     * The active members of a group, the organizer first.
     */
    List<GroupMember> fetchActiveMembers(UUID groupId);

    /**
     * An active member of a group.
     *
     * @param membershipId the membership
     * @param accountId    the account, or null for a member without the application
     * @param displayName  the name of the member
     * @param organizer    true for the organizer
     */
    record GroupMember(UUID membershipId, @Nullable UUID accountId, String displayName, boolean organizer) {
    }
}
