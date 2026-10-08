package pe.kerolabs.pozzo.compliancehistory.application.internal.outboundservices.acl;

import org.springframework.stereotype.Service;
import pe.kerolabs.pozzo.iam.interfaces.acl.IamContextFacade;
import pe.kerolabs.pozzo.savingsgroups.interfaces.acl.SavingsGroupsContextFacade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Anti-corruption layer towards Identity and Access and Savings Groups: the name a member shows,
 * who organizes which group and who belongs to it, translated to what Compliance History needs.
 */
@Service
public class ExternalContextsService {

    private final IamContextFacade iamContextFacade;
    private final SavingsGroupsContextFacade savingsGroupsContextFacade;

    public ExternalContextsService(IamContextFacade iamContextFacade,
                                   SavingsGroupsContextFacade savingsGroupsContextFacade) {
        this.iamContextFacade = iamContextFacade;
        this.savingsGroupsContextFacade = savingsGroupsContextFacade;
    }

    /**
     * Resolves the display name of a member from IAM context.
     *
     * @param accountId the account identifier
     * @return optional display name, or empty if account not found
     */
    public Optional<String> fetchDisplayName(UUID accountId) {
        return iamContextFacade.fetchDisplayNameByAccountId(accountId);
    }

    /**
     * Checks if a given member is the organizer of a savings group.
     *
     * @param groupId the savings group identifier
     * @param accountId the account identifier
     * @return true if the member organizes the group
     */
    public boolean isOrganizer(UUID groupId, UUID accountId) {
        return savingsGroupsContextFacade.isOrganizer(groupId, accountId);
    }

    /**
     * Checks if an organizer organizes any active group containing the given member.
     *
     * @param organizerAccountId the organizer's account identifier
     * @param memberAccountId the member's account identifier
     * @return true if an organizer-member relationship exists in any mutual group
     */
    public boolean isOrganizerOfMember(UUID organizerAccountId, UUID memberAccountId) {
        return savingsGroupsContextFacade.isOrganizerOfMember(organizerAccountId, memberAccountId);
    }

    /**
     * Retrieves the active members of a group translated for compliance history requirements.
     *
     * @param groupId the savings group identifier
     * @return list of active group members with role and display name
     */
    public List<GroupMember> fetchActiveMembers(UUID groupId) {
        return savingsGroupsContextFacade.fetchActiveMembers(groupId).stream()
                .map(member -> new GroupMember(member.membershipId(), member.accountId(), member.displayName(),
                        member.organizer()))
                .toList();
    }

    public record GroupMember(UUID membershipId, UUID accountId, String displayName, boolean organizer) {
    }
}
